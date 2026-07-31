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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.adapter.common.InitException;
import com.lightstreamer.adapter.metadata.CreditsException;
import com.lightstreamer.adapter.metadata.LoginInfo;
import com.lightstreamer.adapter.metadata.MessageException;
import com.lightstreamer.adapter.metadata.MetadataAdapter;
import com.lightstreamer.adapter.metadata.SessionListener;
import com.lightstreamer.adapter.metadata.SessionRequestHandler;
import com.lightstreamer.adapter.metadata.SubscriptionHandler;
import com.lightstreamer.adapter.metadata.SubscriptionInfo;


public class RoundTripMetadataAdapter implements MetadataAdapter {

    /**
     * The associated feed to which messages will be forwarded;
     * it is the Data Adapter itself.
     */
    private volatile RoundTripDataAdapter rtFeed;

    /**
     * The name of the Data Adapter currently associated with rtFeed.
     */
    private volatile String rtFeedName;

    /**
     * Private logger; a specific "LS_demos_Logger.RoundTrip" category
     * should be supplied by log4j configuration.
     */
    private Logger logger;

    @Override
    public void init(Map<String, String> params, File configDir) throws InitException {

        logger = LogManager.getLogger("LS_demos_Logger.RoundTrip");

        logger.info("RoundTripMetadataAdapter ready");
    }

    @Override
    public void onDataAdapterAvailable(String dataAdapterName, Object dataAdapter) {
        if (!(dataAdapter instanceof RoundTripDataAdapter)) {
            // Ignore Data Adapters of different types
            return;
        }
        if (this.rtFeed != null) {
            logger.warn("A RoundTripDataAdapter instance is already available; ignoring the new one: " + dataAdapterName);
            return;
        }
        this.rtFeedName = dataAdapterName;
        this.rtFeed = (RoundTripDataAdapter) dataAdapter;
        logger.info("RoundTripDataAdapter available: " + dataAdapterName);
    }

    @Override
    public void onDataAdapterUnavailable(String dataAdapterName) {
        if (dataAdapterName.equals(this.rtFeedName)) {
            this.rtFeed = null;
            this.rtFeedName = null;
            logger.info("RoundTripDataAdapter unavailable: " + dataAdapterName);
        }
    }

    @Override
    public CompletionStage<SessionRequestHandler> loginAsync(LoginInfo loginInfo) {
        //////////////////////////////////////////////////////////////////////////
        // WARNING: No authentication logic is implemented; any login is accepted.
        // This is suitable for demos only and MUST NOT be used in production.
        //////////////////////////////////////////////////////////////////////////
        Map<String, String> clientContext = loginInfo.getClientContext();

        return CompletableFuture.completedFuture(new SessionRequestHandler() {

            @Override
            public void onStart(SessionListener listener) {
            }

            @Override
            public void onClose() {
            }

            @Override
            public Double getAllowedMaxBandwidth() {
                return null;
            }

            @Override
            public CompletionStage<SubscriptionHandler> newSubscription(SubscriptionInfo info) {
                return CompletableFuture.completedFuture(new SubscriptionHandler(info));
            }

            /**
             * Triggered by a client "sendMessage" call.
             * The message encodes a chat message from the client.
             */
            @Override
            public CompletionStage<String> userMessageAsync(String message)
                    throws CreditsException, MessageException {

                // we won't introduce blocking operations, hence we can proceed inline

                if (message == null) {
                    logger.warn("Null message received");
                    throw new MessageException("Null message received");
                }

                //Split the string on the | character
                //The message must be of the form "RT|n|message" 
                //(where n is the number that identifies the item
                //and message is the message to be published)
                String[] pieces = message.split("\\|", -1);

                if (rtFeed == null) {
                     // The RoundTrip Data Adapter is not available
                     logger.error("RoundTripDataAdapter not found");
                     throw new CreditsException("No roundtrip feed available", 0, "No roundtrip feed available");
                }

                if (pieces.length < 3) {
                    logger.warn("Wrong message received: " + message);
                    throw new MessageException("Wrong message received");
                }

                //read from the client context the IP of the user
                String ip = clientContext.get("REMOTE_IP");

                //Check the message, it must be of the form "RT|n|message"
                //(where n is the number that identifies the item
                //and message is the message to be published)
                if (pieces[0].equals("RT")) {
                    //and send it to the feed
                    if (!rtFeed.sendMessage(ip,pieces[1],message)) {
                         logger.warn("Wrong message received: " + message);
                         throw new MessageException("Wrong message received");
                    }
                } else {
                     logger.warn("Wrong message received: " + message);
                     throw new MessageException("Wrong message received");
                }

                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public void shutdown() {
    }

}