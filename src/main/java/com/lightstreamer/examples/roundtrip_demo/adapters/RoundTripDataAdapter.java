/*
 * Copyright (c) Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightstreamer.examples.roundtrip_demo.adapters;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.adapter.common.InitException;
import com.lightstreamer.adapter.common.Mode;
import com.lightstreamer.adapter.data.DataAdapter;
import com.lightstreamer.adapter.data.ItemListener;
import com.lightstreamer.adapter.data.ItemManager;
import com.lightstreamer.adapter.data.SubscriptionException;


public class RoundTripDataAdapter implements DataAdapter {

    private static final int MAX_MESSAGE_LENGTH = 500;

    /**
     * Private logger; a specific "LS_demos_Logger.RoundTrip" category
     * should be supplied by log4j configuration.
     */
    private Logger logger;

    /**
     * Used to enqueue the calls to the listener.
     */
    private final ExecutorService executor;

    /**
     * A map containing every active subscriptions;
     * It associates each item name with the corresponding item manager.
     */
    private final ConcurrentHashMap<String, RoundTripItemManager> subscriptions =
        new ConcurrentHashMap<String, RoundTripItemManager>();

    public RoundTripDataAdapter() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void init(Map<String, String> params, File configDir) throws InitException {

        // Logging configuration for the demo is carried out in the init
        // method of Metadata Adapter. In order to be sure that this method 
        // is executed after log configuration was completed, this parameter 
        // must be present in the Adapter Set configuration (adapters.xml):
        // <metadata_adapter_initialised_first>Y</metadata_adapter_initialised_first>
        logger = LogManager.getLogger("LS_demos_Logger.RoundTrip");

        // Adapter ready
        logger.info("RounTripDataAdapter ready");

    }

    @Override
    public CompletionStage<ItemManager> getItemManager(String itemName)
            throws SubscriptionException {

        if (!itemName.matches("^roundtrip[01234]$")) {
            //valid items are in the range rountrip0 - roundtrip4
            throw new SubscriptionException("No such item");
        }

        return CompletableFuture.completedFuture(new RoundTripItemManager(itemName));
    }

    private class RoundTripItemManager implements ItemManager {
        private final String itemName;
        private volatile ItemListener listener;
        private HashMap<String,String> snapshot;

        RoundTripItemManager(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public Mode getSupportedMode() {
            return Mode.MERGE;
        }

        @Override
        public CompletionStage<Void> subscribe(ItemListener listener) {

            this.listener = listener;

            // Add the new item to the list of subscribed items
            subscriptions.put(itemName, this);
            
            //send the snapshot
            if (snapshot == null) {
                snapshot = new HashMap<String, String>();
                snapshot.put("message", "-");
                snapshot.put("date", "-");
                snapshot.put("timestamp", "-");
                snapshot.put("IP", "-");
            }
            
            final HashMap<String,String> fUpdate = (HashMap<String,String>) snapshot.clone();

            //If we have a listener create a new Runnable to be used as a task to pass the
            //new update to the listener
            Runnable updateTask = new Runnable() {
                public void run() {
                    // call the update on the listener;
                    // in case the listener has just been detached,
                    // the listener should detect the case
                    listener.snapshot(fUpdate, null);
                }
            };

            //We add the task on the executor to pass to the listener the actual status
            executor.execute(updateTask);
            
            logger.info(itemName + " subscribed");

            return CompletableFuture.completedFuture(null);
        }

        @Override
        public boolean isSnapshotAvailable() {
            //This adapter does not handle the snapshot.
            //If there is someone subscribed the snapshot is kept by the server
            return true;
        }

        @Override
        public CompletionStage<Void> unsubscribe() {

            // Remove from the list of subscribed items
            subscriptions.remove(itemName);
            this.listener = null;

            logger.info(itemName + " unsubscribed");

            return CompletableFuture.completedFuture(null);
        }

        void sendUpdate(String ip, String message) {
            Date now = new Date();
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(now);
            String date = new SimpleDateFormat("yyyy.MM.dd").format(now);

            logger.debug(date + " - " + timestamp + "|New message: " + ip + "->" + itemName + "->" + message);

            if (snapshot == null) {
                //should never happen as the map is generated in the subscribe method
                snapshot = new HashMap<String, String>();
            }
            snapshot.put("message", message);
            snapshot.put("timestamp", timestamp);
            snapshot.put("date", date);
            snapshot.put("IP", ip);

            final HashMap<String, String> fUpdate = (HashMap<String,String>) snapshot.clone();

            final ItemListener itemListener = this.listener;

            //If we have a listener create a new Runnable to be used as a task to pass the
            //new update to the listener
            Runnable updateTask = new Runnable() {
                public void run() {
                    // call the update on the listener;
                    // in case the listener has just been detached,
                    // the listener should detect the case
                    itemListener.update(fUpdate);
                }
            };

            //We add the task on the executor to pass to the listener the actual status
            executor.execute(updateTask);
        }
    }
    
    /**
     * Accepts message submissions
     */
    public boolean sendMessage(String IP, String channel, String message) {
        final String item = "roundtrip"+channel;
        RoundTripItemManager manager = subscriptions.get(item);
        if (manager == null) {
            logger.warn("Received message for wrong or not-subscribed channel");
            return false;
        }

        if (message == null || message.length() == 0) {
            logger.warn("Received empty or null message");
            return false;
        }
        if (IP == null || IP.length() == 0) {
            logger.warn("Received empty or null IP");
            return false;
        }
        
        //strip control characters from message
        message = message.substring(message.indexOf("|",3)+1);
        
        if (message.length() > MAX_MESSAGE_LENGTH) {
            logger.warn("Message length excedes the length limits");
            return false;
        }

        manager.sendUpdate(IP, message);

        return true;
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

}