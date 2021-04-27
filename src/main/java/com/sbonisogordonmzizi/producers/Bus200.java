package com.sbonisogordonmzizi.producers;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Scanner;

public class Bus200{
    public static void main(String... args) {
        busRouteList();
        Scanner scanner = new Scanner(System.in);
        String busID = "Bus200";
        System.out.print("Enter Bus Route :");
        String busroute = scanner.nextLine();
        scanner.close();

        if (busroute.toLowerCase().equals("cityline")) {
            String busTopic = "cityLineBuses";
            String data = "City Line";
            mainLogic(busID,busTopic,data);

        } else if (busroute.toLowerCase().equals("beachline")) {
            String busTopic = "beachLineBuses";
            String data = "Beach Line";
            mainLogic(busID,busTopic,data);
        }else{
            String busTopic = "circleLineBuses";
            String data = "Circle Line";
            mainLogic(busID,busTopic,data);
        }
    }

    public static void mainLogic(final String busID, String busTopic, String data) {

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
        for (int i = 0; i < 100000; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(busTopic, busID, data + " " + i);
            //send data

            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    //execute every time a racord is successfully send or an exception is thrown
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
}