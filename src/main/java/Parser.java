import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Parser {
    public static void main(String[] args) {
        Parser parser = new Parser();
        Map<String, Long> match = parser.getMatches();  //вызов основного метода

        System.out.println("Word : Matches");
        match.forEach((k, v) -> System.out.println(k + " - " + v));
    }

    /*
    * getCollectionCount() - парсит сайт и работает с полученной информацией возвращая мапу
    *                                                       с уникальными словами и их количеством
    * readFile() - читает локальный файл с предложенными словами
    * compareWords(resultMap, inputWords) - возвращает отсортированную мапу в порядке убывания
    *                                                       с уникальными значенями = файлу ввода
    * */
    private Map<String, Long> getMatches() {
        Map<String, Long> resultMap = null;
        List<String> inputWords = null;
        try {
            resultMap = getCollectionCount();
            inputWords = readFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compareWords(resultMap, inputWords);
    }

    /*
    * getDocument() - запрашивает страницу со всем содержимым
    * getResultList(doc) - нарезает в массив строк убирая лишние символы + приводит к нижнему регистру
    * return resultList стрим возвращает мапу с уникальными словами и их количеством
    * */
    private Map<String, Long> getCollectionCount() throws IOException {
        Document doc = getDocument();
        List<String> resultList = getResultList(doc);
        return resultList
                .stream()
                .collect(Collectors
                        .groupingBy(Function.identity(), Collectors.counting()));
    }

    private Document getDocument() throws IOException {
        return Jsoup.connect("https://news.yandex.ru/computers.html")
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Safari/605.1.15")
                .referrer("http://www.google.com")
                .get();
    }

    private List<String> getResultList(Document doc) {
        return Arrays.asList(doc
                .getAllElements()
                .text()
                .replaceAll("[^a-zA-Zа-яА-Я]", " ")
                .toLowerCase()
                .split("\\s+"));
    }

    /*
    * чтение из файла и запись в лист
    * */
    private List<String> readFile() throws IOException {
        List<String> inputWords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/testData.txt"))) {
            while (br.ready()) {
                inputWords.add(br.readLine());
            }
        }
        return inputWords;
    }

    /*
    * находит и записывает equals слова в новую мапу в качестве ключа, value - количество
    * doSort(matchingMap) - сортировка по значению в порядке убывания
    * */
    private Map<String, Long> compareWords(Map<String, Long> resultMap, List<String> inputWords) {
        Map<String, Long> matchingMap = new HashMap<>();
        for (String s : inputWords) {
            matchingMap.put(s, resultMap.getOrDefault(s.toLowerCase(), 0L));
        }
        return doSort(matchingMap);
    }

    /*
    * стрим формирует упорядоченную мапу(реверсивную по значениям)
    * */
    private Map<String, Long> doSort(Map<String, Long> matchingMap) {
        return matchingMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (v1, v2) -> v1, LinkedHashMap::new));
    }
}
