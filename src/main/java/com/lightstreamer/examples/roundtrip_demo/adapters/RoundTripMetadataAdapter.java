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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.adapters.metadata.LiteralBasedProvider;
import com.lightstreamer.interfaces.metadata.CreditsException;
import com.lightstreamer.interfaces.metadata.MetadataProviderException;
import com.lightstreamer.interfaces.metadata.NotificationException;


public class RoundTripMetadataAdapter extends LiteralBasedProvider {

    /**
     * The associated feed to which messages will be forwarded;
     * it is the Data Adapter itself.
     */
    private volatile RoundTripDataAdapter rtFeed;

    /**
     * Unique identification of the related RoundTrip Data Adapter instance;
     * see feedMap on the RoundTripDataAdapter.
     */
    private String adapterSetId;

    /**
     * Private logger; a specific "LS_demos_Logger.RoundTrip" category
     * should be supplied by log4j configuration.
     */
    private Logger logger;

    /**
     * Keeps the client context information supplied by Lightstreamer on the
     * new session notifications.
     * Session information is needed to pass the IP to the RoundTrip Data Adapter.
     */
    private ConcurrentHashMap<String,Map<String,String>> sessions = new ConcurrentHashMap<String,Map<String,String>>();

    public RoundTripMetadataAdapter() {
    }

    public void init(Map params, File configDir) throws MetadataProviderException {
        //Call super's init method to handle basic Metadata Adapter features
        super.init(params,configDir);

        logger = LogManager.getLogger("LS_demos_Logger.RoundTrip");

        // Read the Adapter Set name, which is supplied by the Server as a parameter
        this.adapterSetId = (String) params.get("adapters_conf.id");

        /*
         * Note: the RoundTripDataAdapter instance cannot be looked for
         * here to initialize the "rtFeed" variable, because the RoundTrip
         * Data Adapter may not be loaded and initialized at this moment.
         * We need to wait until the first "sendMessage" occurrence;
         * then we can store the reference for later use.
         */

        logger.info("RoundTripMetadataAdapter ready");
    }

    /**
     * Triggered by a client "sendMessage" call.
     * The message encodes a chat message from the client.
     */
    public void notifyUserMessage(String user, String session, String message)
        throws NotificationException, CreditsException {

        if (message == null) {
            logger.warn("Null message received");
            throw new NotificationException("Null message received");
        }

        //Split the string on the | character
        //The message must be of the form "RT|n|message" 
        //(where n is the number that identifies the item
        //and message is the message to be published)
        String[] pieces = message.split("\\|", -1);

        this.loadRTFeed();
        this.handleRTMessage(pieces,message,session);
    }

    public void notifyNewSession(String user, String session, Map sessionInfo)
            throws CreditsException, NotificationException {

        //we can't have duplicate sessions
        assert(!sessions.containsKey(session));

        // Register the session details on the sessions HashMap.
        sessions.put(session, sessionInfo);

    }

    public void notifySessionClose(String session) throws NotificationException {
        //the session must exist to be closed
        assert(sessions.containsKey(session));

        //we have to remove session information from the session HashMap
        sessions.remove(session);
    }

    private void loadRTFeed() throws CreditsException {
        if (this.rtFeed == null) {
             try {
                 // Get the RoundTripDataAdapter instance to bind it with this
                 // Metadata Adapter and send chat messages through it
                 this.rtFeed = RoundTripDataAdapter.feedMap.get(this.adapterSetId);
             } catch(Throwable t) {
                 // It can happen if the RoundTrip Data Adapter jar was not even
                 // included in the Adapter Set lib directory (the RoundTrip
                 // Data Adapter could not be included in the Adapter Set as well)
                 logger.error("RoundTripDataAdapter class was not loaded: " + t);
                 throw new CreditsException(0, "No roundtrip feed available", "No roundtrip feed available");
             }

             if (this.rtFeed == null) {
                 // The feed is not yet available on the static map, maybe the
                 // RoundTrip Data Adapter was not included in the Adapter Set
                 logger.error("RoundTripDataAdapter not found");
                 throw new CreditsException(0, "No roundtrip feed available", "No roundtrip feed available");
             }
        }
    }

    private void handleRTMessage(String[] pieces, String message, String session) throws NotificationException {
        if (pieces.length < 3) {
            logger.warn("Wrong message received: " + message);
            throw new NotificationException("Wrong message received");
        }

        //extract session info
        Map<String,String> sessionInfo = sessions.get(session);
        if (sessionInfo == null) {
             logger.warn("Message received from non-existent session: " + message);
             throw new NotificationException("Wrong message received");
        }
        //read from info the IP of the user
        String ip =  sessionInfo.get("REMOTE_IP");

        //Check the message, it must be of the form "RT|n|message"
        //(where n is the number that identifies the item
        //and message is the message to be published)
        if (pieces[0].equals("RT")) {
            //and send it to the feed
            if (!this.rtFeed.sendMessage(ip,pieces[1],message)) {
                 logger.warn("Wrong message received: " + message);
                 throw new NotificationException("Wrong message received");
            }
        } else {
             logger.warn("Wrong message received: " + message);
             throw new NotificationException("Wrong message received");
        }

    }

}