package week1.soojendus;

import java.util.*;

public class Exercise3 {

    /**
     * Defineeri meetod eval, mis väärtustab etteantud avaldise.
     * @param str on plussidega eraldatud arvude jada, näiteks "5 + 35+  10".
     * @return arvude summa, antud näide puhul 50.
     */
    public static int eval(String str) {
        str = str.replace(" ", "");
        String[] parts = str.split("\\+");
        int sum = 0;
        for (String part : parts) {
            sum += Integer.parseInt(part);
        }
        return sum;
    }

    /**
     * Tuletame lihtsalt meelde Java List ja Map andmestruktuurid!
     * Selle ülesanne puhul võiks tegelikult tüüpide ja main meetodi põhjal aru saada, mida tegema peaks...
     *
     * @param list sõnedest, kus on vaheldumisi nimi ja arv (sõne kujul). Võib eeldada, et pikkus on paarisarv.
     * @return listile vastav map nimedest arvudesse.
     */
    public static Map<String, Integer> createMap(List<String> list) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < list.size(); i += 2) {
            map.put(list.get(i), Integer.parseInt(list.get(i + 1)));
        }
        return map;
    }

    public static void main(String[] args) {
        System.out.println(eval("2+2"));
        Map<String, Integer> ageMap = createMap(Arrays.asList("Carmen", "17", "Jürgen", "44", "Tarmo", "10", "Mari", "83"));
        System.out.println(ageMap.get("Carmen")); // vastus: 17
        System.out.println(ageMap.get("Tarmo"));  // vastu: 10

        for (Map.Entry<String, Integer> entry : ageMap.entrySet()) {
            System.out.println(entry.getKey() + " on " + entry.getValue() + " aastane.");
        }
    }
}
