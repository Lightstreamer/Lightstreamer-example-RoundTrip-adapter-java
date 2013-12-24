# Lightstreamer - Round-Trip Demo - Java Adapter #
<!-- START DESCRIPTION Round-Trip Demo -->

This project shows the Round-Trip Demo Data and Metadata Adapters and how they can be plugged into Lightstreamer Server and used to feed the [Lightstreamer - Round-Trip Demo - HTML Client](https://github.com/Weswit/Lightstreamer-example-RoundTrip-client-javascript) front-end. Please refer [here](http://www.lightstreamer.com/latest/Lightstreamer_Allegro-Presto-Vivace_5_1_Colosseo/Lightstreamer/DOCS-SDKs/General%20Concepts.pdf) for more details about Lightstreamer Adapters.
The [Lightstreamer - Round-Trip Demo - HTML Client](https://github.com/Weswit/Lightstreamer-example-RoundTrip-client-javascript) is a simple broadcast messages application based on Lightstreamer.

The project is comprised of source code and a deployment example. The source code is divided into two folders.

## Round-Trip Data Adapter - src_roundtrip ##
Contains the source code for the Round-Trip Data Adapter. This Adapter broadcasts the values of received from the Metadata Adapter for the five defined items in real time.<br>

## Metadata Adapter - src_metadata ##
Contains the source code for a Metadata Adapter to be associated with the Round-Trip Demo Data Adapter. This Metadata Adapter inherits from the reusable `LiteralBasedProvider` in [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java) and it plays the extra-role of receiving messages from the clients and forwarding them to the Data Adapter.<br>
It should not be used as a reference for a real case of client-originated message handling, as no guaranteed delivery and no clustering support is shown.
<br>
See the source code comments for further details.
<!-- END DESCRIPTION Round-Trip Demo -->

# Build #

If you want to skip the build process of this Adapter please note that in the [deploy](https://github.com/Weswit/Lightstreamer-example-RoundTrip-adapter-java/releases) release of this project you can find the "deploy.zip" file that contains a ready-made deployment resource for the Lightstreamer server.
Otherwise follow these steps:

*  Get the ls-adapter-interface.jar, ls-generic-adapters.jar, and log4j-1.2.15.jar files from the [latest Lightstreamer distribution](http://www.lightstreamer.com/download).
*  Create the jars LS_roundtrip_metadata_adapter.jar and LS_roundtrip_data_adapter.jar with commands like these:
```sh
 >javac -source 1.7 -target 1.7 -nowarn -g -classpath compile_libs/log4j-1.2.15.jar;compile_libs/ls-adapter-interface/ls-adapter-interface.jar;compile_libs/ls-generic-adapters/ls-generic-adapters.jar -sourcepath src/src_data -d tmp_classes src/src_roundtrip/roundtrip_demo/adapters/RoundTripDataAdapter.java
 
 >jar cvf LS_roundtrip_data_adapter.jar -C tmp_classes src_data
 
 >javac -source 1.7 -target 1.7 -nowarn -g -classpath compile_libs/log4j-1.2.15.jar;compile_libs/ls-adapter-interface/ls-adapter-interface.jar;compile_libs/ls-generic-adapters/ls-generic-adapters.jar;LS_messenger_data_adapter.jar -sourcepath src/src_metadata -d tmp_classes src/src_metadata/roundtrip_demo/adapters/RoundTripMetadataAdapter.java
 
 >jar cvf LS_roundtrip_metadata_adapter.jar -C tmp_classes src_metadata
```

# Deploy #

Now you are ready to deploy the Round-Trip Demo Adapter into Lighstreamer server.<br>
After you have Downloaded and installed Lightstreamer, please go to the "adapters" folder of your Lightstreamer Server installation. You should find a "Demo" folder containing some adapters ready-made for several demos. You have to remove the "Demo" folder if you want to install the Round-Trip Adapter Set alone. 
Please follow the below steps to configure the Round-Trip Adapter Set properly.

You have to create a specific folder to deploy the Round-Trip Adapters otherwise get the ready-made "roundtrip" deploy folder from "deploy.zip" of the [latest release](https://github.com/Weswit/Lightstreamer-example-RoundTrip-adapter-java/releases) of this project and skips the next three steps.

1. Create a new folder, let's call it "roundtrip", and a "lib" folder inside it.
2. Create an "adapters.xml" file inside the "roundtrip" folder and use the following content (this is an example configuration, you can modify it to your liking):

```xml      
  <?xml version="1.0"?>

  <!-- Mandatory. Define an Adapter Set and sets its unique ID. -->
  <adapters_conf id="ROUNDTRIPDEMO">

    <!-- Mandatory. Define the Metadata Adapter. -->
    <metadata_provider>

        <!-- Mandatory. Java class name of the adapter. -->
        <adapter_class>roundtrip_demo.adapters.RoundTripMetadataAdapter</adapter_class>

        <!-- Optional for RoundTripMetadataAdapter.
             Configuration file for the Adapter's own logging.
             Logging is managed through log4j. -->
        <param name="log_config">adapters_log_conf.xml</param>
        <param name="log_config_refresh_seconds">10</param>

        <!-- Optional, managed by the inherited LiteralBasedProvider.
             See LiteralBasedProvider javadoc. -->
        <!--
        <param name="max_bandwidth">40</param>
        <param name="max_frequency">3</param>
        <param name="buffer_size">30</param>
        <param name="prefilter_frequency">5</param>
        <param name="allowed_users">user123,user456</param>
        <param name="distinct_snapshot_length">30</param>
        -->

        <!-- Optional, managed by the inherited LiteralBasedProvider.
             See LiteralBasedProvider javadoc. -->
        <param name="item_family_1">roundtrip\d{1,2}</param>
        <param name="modes_for_item_family_1">MERGE</param>

    </metadata_provider>


    <data_provider name="ROUNDTRIP">

        <!-- Mandatory. Java class name of the adapter. -->
        <adapter_class>roundtrip_demo.adapters.RoundTripDataAdapter</adapter_class>

        <!-- Optional for RoundTripDataAdapter.
             Configuration file for the Adapter's own logging.
             Leans on the Metadata Adapter for the configuration refresh.
             Logging is managed through log4j. -->
        <param name="log_config">adapters_log_conf.xml</param>

    </data_provider>

</adapters_conf>
```
<br> 
3. Copy into "/roundtrip/lib" the jars (LS_roundtrip_metadata_adapter.jar and LS_roundtrip_data_adapter.jar) created in the previous section.

Now your "roundtrip" folder is ready to be deployed in the Lightstreamer server, please follow these steps:<br>

1. Make sure you have installed Lightstreamer Server, as explained in the GETTING_STARTED.TXT file in the installation home directory.
2. Make sure that Lightstreamer Server is not running.
3. Copy the "roundtrip" directory and all of its files to the "adapters" subdirectory in your Lightstreamer Server installation home directory.
4. Copy the "ls-generic-adapters.jar" file from the "lib" directory of the sibling "Reusable_MetadataAdapters" SDK example to the "shared/lib" subdirectory in your Lightstreamer Server installation home directory.
5. Lightstreamer Server is now ready to be launched.

Please test your Adapter with one of the clients in the [list](https://github.com/Weswit/Lightstreamer-example-RoundTrip-adapter-java#clients-using-this-adapter) below.

# See Also #

## Clients using this Adapter ##
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Round-Trip Demo - HTML Client](https://github.com/Weswit/Lightstreamer-example-RoundTrip-client-javascript)
* [Lightstreamer - Basic Stock-List and Round-trip Demo - Java ME Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-midlet)

<!-- END RELATED_ENTRIES -->

## Related projects ##

* [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java)
* [Lightstreamer - Basic Chat Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Chat-adapter-java)
* [Lightstreamer - Basic Chat Demo - HTML Client](https://github.com/Weswit/Lightstreamer-example-Chat-client-javascript)

# Lightstreamer Compatibility Notes #

- Compatible with Lightstreamer SDK for Java Adapters since 5.1
