package com.sbonisogordonmzizi.producers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class Bus600city {
    public static void main(String... args) {
        busRouteList();
        Scanner scanner = new Scanner(System.in);
        String busID = "Bus600city";
        System.out.print("Enter Bus Route :");
        String busroute = scanner.nextLine();
        scanner.close();

        if (busroute.toLowerCase().equals("cityline")) {
            String busTopic = "cityLineBuses";
            String filename= "/home/viceblack/Programming/JavaProjects/cityBusTracker/src/main/resources/cityLineGPSCoordinates.json";
            mainLogic(busID,busTopic,filename);

        } else if (busroute.toLowerCase().equals("beachline")) {
            String busTopic = "beachLineBuses";
            String filename= "/home/viceblack/Programming/JavaProjects/cityBusTracker/src/main/resources/beachLineGPSCoordinates.json";
            mainLogic(busID,busTopic,filename);
        }else{
            String busTopic = "circleLineBuses";
            String filename = "/home/viceblack/Programming/JavaProjects/cityBusTracker/src/main/resources/circleLineGPSCoordinates.json";
            mainLogic(busID,busTopic,filename);
        }
    }

    public static void mainLogic(final String busID, String busTopic, String filename) {

        String bootstrapServers = "127.0.0.1:9092";
        //create Producer Properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //create the producer
        final KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        final KafkaProducer<String, String > logsProducer = new KafkaProducer<String, String>(properties);
        final KafkaProducer<String,String> exceptionProducer = new KafkaProducer<String, String>(properties);
        //Create a producer record
        //get bus gps coordinates as arrayList<String>
        ArrayList<String> busGPS = getGPSCoordinates(filename);
        for (String data : busGPS) {
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(busTopic, busID, data+" "+busID);
            //ProducerRecord<String, String> record = new ProducerRecord<String, String>(busTopic, busID, data);
            //send data

            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    //execute every time a record is successfully send or an exception is thrown
                    if(e == null){
                        String recordData = "BusID : "+busID+" Topic : " +recordMetadata.topic()+" Partition : "
                                +recordMetadata.partition()+" Offset "+recordMetadata.offset()+
                                " TimeStamp : "+recordMetadata.timestamp();
                        ProducerRecord<String, String> recordSentSuccessfully = new ProducerRecord<String, String>("busLogs",  recordData);
                        logsProducer.send(recordSentSuccessfully);
                        logsProducer.flush();

                    }else{
                        String errorData = e.getStackTrace()+"";
                        ProducerRecord<String, String> recodeSentUnSuccessfully = new ProducerRecord<String, String>("busErrorLogs",  errorData);
                        exceptionProducer.send(recodeSentUnSuccessfully);
                        exceptionProducer.flush();
                    }
                }
            });
            //flush data to topic
            producer.flush();
            try {
                Thread.sleep(1500);
            }catch (InterruptedException e1){
                e1.printStackTrace();
            }

        }
        //close a producer
        producer.close();
        logsProducer.close();
        exceptionProducer.close();
    }

    public static void busRouteList(){
        System.out.println("___ Bus Route List __");
        System.out.println("\tCityLine");
        System.out.println("\tBeachLine");
        System.out.println("\tCircleLine\n");
    }


    public static ArrayList<String> getGPSCoordinates(String filename){
        ArrayList<String> gpsCoordinates = new ArrayList<String>();
        try {
            File input = new File(filename);
            JsonElement fileElement = JsonParser.parseReader(new FileReader(input));
            JsonObject fileObject = fileElement.getAsJsonObject();

            //Extracting the Bus GPS Coordinates from json source
            //get all the keys
            for(String key1 : fileObject.keySet()){
                //check if the returned values are JsonArray and not null
                if(fileObject.get(key1).isJsonArray()) {
                    //get any JsonArray
                    JsonArray jsonArray1 = fileObject.get(key1).getAsJsonArray();
                    //loop JsonArray elements
                    for(JsonElement jsonElement1 : jsonArray1) {
                        //convert JsonElements into JsonObject
                        JsonObject jsonObject1 = jsonElement1.getAsJsonObject();
                        //list all keys
                        for(String key2 : jsonObject1.keySet()){
                            //access values of those keys
                            if(jsonObject1.get(key2).isJsonObject()) {
                                JsonObject jsonObject2 = jsonObject1.get(key2).getAsJsonObject();
                                for(String key3 : jsonObject2.keySet()){
                                    if(jsonObject2.get(key3).isJsonArray()) {
                                        JsonArray jsonArray2 = jsonObject2.get(key3).getAsJsonArray();
                                        for(JsonElement jsonElement2 : jsonArray2){
                                            gpsCoordinates.add(jsonElement2.toString());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        }
        return gpsCoordinates;
    }
}

