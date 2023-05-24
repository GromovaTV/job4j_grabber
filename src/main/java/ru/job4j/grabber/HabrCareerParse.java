package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HarbCareerDateTimeParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HabrCareerParse implements Parse{
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        try {
            Connection cnDesc = Jsoup.connect(link);
            Document document = cnDesc.get();
            List<String> list = new LinkedList<>();
            Elements container = document.select(".basic-section--appearance-vacancy-description");
            Elements h2 = container.select("> h2");
            list.add(h2.text());
            Element div = container.select(".vacancy-description__text").first();
            Elements divEl = div.select("> *");
            for (Element el : divEl) {
                list.add(el.text());
            }
            String description = String.join(System.lineSeparator(), list);
            return description;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HarbCareerDateTimeParser());
        var list = habrCareerParse.list(PAGE_LINK);
        list.forEach(System.out::println);

    }

    @Override
    public List<Post> list(String path) {
        List<Post> posts = new ArrayList<>();
        int id = 0;
        for (int pageNumber = 1; pageNumber < 5; pageNumber++) {
            String pageLink = String.format("%s?page=%d", path, pageNumber);
            System.out.printf("Page №%d%n", pageNumber);
            try {
                Connection connection = Jsoup.connect(pageLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                for (Element row : rows) {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                    String description = this.retrieveDescription(link);
                    String dateTime = row.select(".basic-date").first().attr("datetime");
                    Post post = new Post(id++, vacancyName, link, description, dateTimeParser.parse(dateTime));
                    posts.add(post);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return posts;
    }
}
