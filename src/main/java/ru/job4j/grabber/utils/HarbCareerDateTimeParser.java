package ru.job4j.grabber.utils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class HarbCareerDateTimeParser implements DateTimeParser {
    @Override
    public LocalDateTime parse(String parse) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        LocalDateTime dateTime = LocalDateTime.parse(parse, formatter);
        return dateTime;
    }

    public static void main(String[] args) {
        HarbCareerDateTimeParser parser = new HarbCareerDateTimeParser();
        LocalDateTime p= parser.parse("2023-05-23T16:27:03+03:00");
        System.out.println(p.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}