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

package roundtrip_demo.adapters;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.interfaces.data.DataProviderException;
import com.lightstreamer.interfaces.data.FailureException;
import com.lightstreamer.interfaces.data.ItemEventListener;
import com.lightstreamer.interfaces.data.SmartDataProvider;
import com.lightstreamer.interfaces.data.SubscriptionException;


public class RoundTripDataAdapter implements SmartDataProvider {

    /**
     * A static map, to be used by the Metadata Adapter to find the data
     * adapter instance; this allows the Metadata Adapter to forward client
     * messages to the adapter.
     * The map allows multiple instances of this Data Adapter to be included
     * in different Adapter Sets. Each instance is identified with the name
     * of the related Adapter Set; defining multiple instances in the same
     * Adapter Set is not allowed.
     */
    public static final ConcurrentHashMap<String, RoundTripDataAdapter> feedMap =
        new ConcurrentHashMap<String, RoundTripDataAdapter>();

    private static final int MAX_MESSAGE_LENGTH = 500;

    /**
     * Private logger; a specific "LS_demos_Logger.RoundTrip" category
     * should be supplied by log4j configuration.
     */
    private Logger logger;

    /**
     * The listener of updates set by Lightstreamer Kernel.
     */
    private ItemEventListener listener;

    /**
     * Used to enqueue the calls to the listener.
     */
    private final ExecutorService executor;

    /**
     * A map containing every active subscriptions;
     * It associates each item name with the item handle to be used
     * to identify the item towards Lightstreamer Kernel.
     */
    private final ConcurrentHashMap<String, Object> subscriptions =
        new ConcurrentHashMap<String, Object>();
    
    private final ConcurrentHashMap<String, HashMap<String,String>> snapshots =
        new ConcurrentHashMap<String, HashMap<String,String>>();

    public RoundTripDataAdapter() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void init(Map params, File configDir) throws DataProviderException {

        // Logging configuration for the demo is carried out in the init
        // method of Metadata Adapter. In order to be sure that this method 
        // is executed after log configuration was completed, this parameter 
        // must be present in the Adapter Set configuration (adapters.xml):
        // <metadata_adapter_initialised_first>Y</metadata_adapter_initialised_first>
        logger = LogManager.getLogger("LS_demos_Logger.RoundTrip");

        // Read the Adapter Set name, which is supplied by the Server as a parameter
        String adapterSetId = (String) params.get("adapters_conf.id");

        // Put a reference to this instance on a static map
        // to be read by the Metadata Adapter
        feedMap.put(adapterSetId, this);

        // Adapter ready
        logger.info("RounTripDataAdapter ready");

    }

    public void subscribe(String item, Object handle, boolean arg2)
            throws SubscriptionException, FailureException {

        assert(! subscriptions.containsKey(item));

        if (!item.matches("^roundtrip[01234]$")) {
            //valid items are in the range rountrip0 - roundtrip4
            throw new SubscriptionException("No such item");
        }

        // Add the new item to the list of subscribed items
        subscriptions.put(item, handle);
        
        //send the snapshot
        sendSnapshot(item);
        
        logger.info(item + " subscribed");

    }
    
    public void sendSnapshot(String item) {
        final Object handle = subscriptions.get(item);

        HashMap<String,String> update = snapshots.get(item);
        if (update == null) {
            update = new HashMap<String, String>();
            update.put("message", "-");
            update.put("date", "-");
            update.put("timestamp", "-");
            update.put("IP", "-");
            snapshots.put(item, update);
        }
        
        final HashMap<String,String> fUpdate = (HashMap<String,String>) update.clone();

        //If we have a listener create a new Runnable to be used as a task to pass the
        //new update to the listener
        Runnable updateTask = new Runnable() {
            public void run() {
                // call the update on the listener;
                // in case the listener has just been detached,
                // the listener should detect the case
                listener.smartUpdate(handle, fUpdate, true);
            }
        };

        //We add the task on the executor to pass to the listener the actual status
        executor.execute(updateTask);
    }

    public void unsubscribe(String item) throws SubscriptionException,
        FailureException {

        assert(subscriptions.containsKey(item));

        // Remove the handle from the list of subscribed items
        subscriptions.remove(item);

        logger.info(item + " unsubscribed");
    }

    public boolean isSnapshotAvailable(String arg0)
            throws SubscriptionException {
        //This adapter does not handle the snapshot.
        //If there is someone subscribed the snapshot is kept by the server
        return true;
    }


    public void setListener(ItemEventListener listener) {
        this.listener = listener;
    }
    
    /**
     * Accepts message submissions
     */
    public boolean sendMessage(String IP, String channel, String message) {
        final String item = "roundtrip"+channel;
        if (!subscriptions.containsKey(item)) {
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
        
        Date now = new Date();
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(now);
        String date = new SimpleDateFormat("yyyy.MM.dd").format(now);

        logger.debug(date + " - " + timestamp + "|New message: " + IP + "->" + channel + "->" + message);

        HashMap<String,String> update = snapshots.get(item);
        if (update == null) {
            //should never happen as the map is generated in the sendSnapshot method 
            update = new HashMap<String, String>();
            snapshots.put(item, update);            
        }
        update.put("message", message);
        update.put("timestamp", timestamp);
        update.put("date", date);
        update.put("IP", IP);
        

        
        final HashMap<String, String> fUpdate = (HashMap<String,String>) update.clone();

        //If we have a listener create a new Runnable to be used as a task to pass the
        //new update to the listener
        Runnable updateTask = new Runnable() {
            public void run() {
                // call the update on the listener;
                // in case the listener has just been detached,
                // the listener should detect the case
                listener.smartUpdate(subscriptions.get(item), fUpdate, false);
            }
        };

        //We add the task on the executor to pass to the listener the actual status
        executor.execute(updateTask);

        return true;
    }

    public void subscribe(String arg0, boolean arg1)
        throws SubscriptionException, FailureException {
    //NEVER CALLED

    }

}