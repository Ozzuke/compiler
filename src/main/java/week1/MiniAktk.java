package week1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * MiniAktk on väike keel, millel on vaid kolm funktsionaalsust:
 * - print, mis prindib ekraanile ühe arvu
 * - avaldiste arvutamine, mis koosnevad täisarvudest ja operaatoritest + ja -
 * - muutujad, mis on ühe tähemärgi pikkused
 */
public class MiniAktk {
    private final Map<String, Integer> vars;

    public static void main(String[] args) throws Exception {
        var read = Files.readAllLines(Path.of(args[0]));
        read.replaceAll(s -> s.split("#")[0].trim()); // kommentaarid
        read.removeIf(String::isBlank); // tühjad read

        var comp = new MiniAktk();
        try {
            for (var s : read) {
                if (s.startsWith("print ")) {
                    System.out.println(comp.eval(s.substring(6), false));
                } else {
                    comp.eval(s, true);
                }
            }
        } catch (Exception e) {
            System.err.println("Viga: " + e.getMessage());
        }
    }

    public MiniAktk() {
        vars = new HashMap<>();
    }

    public int eval(String s, Boolean luba_väärtustamine) throws Exception {
        s = removeSpaces(s); // eemaldame tühikud
        if (s.contains("=")) { // kui tegu muutuja defineerimisega
            if (!luba_väärtustamine) // kui parajasti prindime midagi, siis ei saa
                throw new Exception("Printides ei tohi muutujatele väärtusi omistada");
            String[] split = s.split("=");
            if (split.length != 2) // peab olema täpselt üks võrdusmärk
                throw new Exception("Vale arv võrdusmärke");
            if (split[0].length() != 1) // muutuja nimi peab olema ühetäheline
                throw new Exception("Muutuja nimi peab olema täpselt ühe tähemärgi pikkune");
            if (!Character.isLetter(split[0].charAt(0))) // muutuja nimi peab olema täht
                throw new Exception("Muutuja nimi peab olema täht");
            vars.put(split[0], eval(split[1], false)); // salvestame muutuja väärtuse
            return vars.get(split[0]); // tagastame selle sest why not (midagi peab tagastama)
        }

        List<StringBuilder> parts = new ArrayList<>(); // jagame avaldise osadeks
        parts.add(new StringBuilder());
        int i = 0; // sümboli indeks
        int type = 0; // 0 - operaator, 1 - number, 2 - muutuja

        while (i < s.length()) {
            char c = s.charAt(i); // vaatame järgmist sümbolit
            int newType = getTypeId(c); // ja selle tüüpi
            if (newType == type) { // kui sama tüüpi mis varem
                if (newType == 0) // operaator ei tohi olla alguses ega järjestikku
                    throw new Exception("Operaator peab olema arvude vahel");
                if (newType == 2) // muutuja peab olema ühetäheline
                    throw new Exception("Muutuja nimi peab olema täpselt ühe tähemärgi pikkune");
                parts.getLast().append(c); // kui tegu numbriga, siis lisame selle olemasolevale
            } else {
                if (type + newType >= 3) // kui number ja muutuja koos (1+2=3)
                    throw new Exception("Arvude vahel peab olema operaator");
                if (newType == 2 && !vars.containsKey(String.valueOf(c))) // muutuja peab olema defineeritud
                    throw new Exception("Tundmatu muutuja");
                parts.add(new StringBuilder()); // uus osa (number, muutuja või operaator)
                parts.getLast().append(c); // lisame selle just loodud osasse
            }
            type = newType; // uuendame tüüpi
            i++;
        }
        parts.removeIf(StringBuilder::isEmpty); // eemaldame tühjad osad

        while (parts.size() >= 3) { // kuni on veel midagi arvutada
            int a, b;
            if (parts.getFirst().charAt(parts.getFirst().length() - 1) >= '0' && parts.getFirst().charAt(parts.getFirst().length() - 1) <= '9') // kui number
                // kui tegu numbriga (viimane sümbol on number, esimene võib olla miinus)
                a = Integer.parseInt(parts.getFirst().toString());
            else // kui tegu muutujaga
                a = vars.get(parts.get(0).toString());
            if (parts.get(2).charAt(0) >= '0' && parts.get(2).charAt(0) <= '9') // kui number
                b = Integer.parseInt(parts.get(2).toString());
            else
                b = vars.get(parts.get(2).toString());
            int result;
            if (parts.get(1).charAt(0) == '+') // liitmine
                result = a + b;
            else // lahutamine
                result = a - b;
            parts.removeFirst(); // eemaldame esimese osa
            parts.removeFirst(); // eemaldame teise osa
            parts.set(0, new StringBuilder(String.valueOf(result))); // asendame tulemusega
        }

        if (parts.size() != 1) { // peaks olema ainult üks osa alles
            System.out.println(parts);
            throw new Exception("Vale arv operaatoreid");
        }
        if (parts.getFirst().charAt(parts.getFirst().length() - 1) >= '0' && parts.getFirst().charAt(parts.getFirst().length() - 1) <= '9') // kui number
            return Integer.parseInt(parts.getFirst().toString()); // tagastame tulemuse
        return vars.get(parts.getFirst().toString()); // tagastame muutuja väärtuse
    }

    /**
     * Tagastab sümboli tüübi (0 - operaator, 1 - number, 2 - muutuja)
     *
     * @param c sümbol
     * @return sümboli tüüp
     */
    private int getTypeId(char c) {
        return switch (c) {
            case '+', '-' -> 0;
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> 1;
            default -> 2;
        };
    }

    /**
     * Tegeleb tühikutest vabanemisega
     * Viskab errori näiteks juhul kui kaks numbrit vms on järjest aga tühikuga eraldatud
     * Ühtlasi viskab errori, kui kasutusel on mõni muu sümbol peale +, -, =, 0-9 ja a-zA-Z
     *
     * @param s sisend
     * @return sisend ilma tühikuteta
     */
    private String removeSpaces(String s) throws Exception {
        s = s.trim();
        StringBuilder sb = new StringBuilder();
        int i = 0; // sümboli indeks
        boolean lastWasOperator = true; // kas eelmine sümbol oli operaator
        boolean space = false; // kas eelmine sümbol oli tühik
        while (i < s.length()) {
            char c = s.charAt(i);
            boolean isOperator = c == '+' || c == '-' || c == '=';
            if (c == ' ')
                space = true;
            else { // kui ei ole tühik
                if (space && !lastWasOperator && !isOperator)
                    throw new Exception("Tühikud numbritest või muutujatest eraldavad");
                if (!isOperator && !Character.isLetterOrDigit(c))
                    throw new Exception("Vale sümbol");
                sb.append(c);
                lastWasOperator = isOperator;
                space = false;
            }
            i++;
        }
        return sb.toString();
    }
}
