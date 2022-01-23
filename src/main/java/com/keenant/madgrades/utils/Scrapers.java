package com.keenant.madgrades.utils;

import com.keenant.madgrades.Constants;
import com.keenant.madgrades.data.Subject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Scrapers {
    public static Set<Subject> scrapeSubjects() throws IOException {
        Set<Subject> subjects = new HashSet<>();
        Document doc = Jsoup.connect(Constants.SUBJECTS_URL).get();

        Element tbody = doc.selectFirst("table").selectFirst("tbody");

        for (Element tr : tbody.select("tr")) {
            Elements children = tr.children();

            String name = children.get(0).text();
            String abbreviation = children.get(1).text();
            String code = children.get(2).text();

            subjects.add(new Subject(name, abbreviation, code));
        }

        return subjects;
    }

    public static Map<Integer, String> scrapeDirReports(String registrarReports) {
        File dirReportPath = new File(registrarReports, "dir");
        if (!dirReportPath.isDirectory()) {
            System.out.println(dirReportPath + " is not a directory");
        }
        Map<Integer, String> result = new HashMap<>();
        for (File pdfPath : dirReportPath.listFiles()) {
            // check if name is seperated by '-' or '_'
            String splitRegex = "-";
            if (pdfPath.getName().contains("_")) {
                splitRegex = "_";
            }
            int termCode = Integer.parseInt(pdfPath.getName().split(splitRegex)[0]);
            result.put(termCode, pdfPath.getAbsolutePath());
        }
        return result;
    }

    public static Map<Integer, String> scrapeGradeReports(String registrarReports) {
        File gradeReportPath = new File(registrarReports, "grades");
        if (!gradeReportPath.isDirectory()) {
            System.out.println(gradeReportPath + " is not a directory");
        }
        Map<Integer, String> result = new HashMap<>();
        for (File pdfPath : gradeReportPath.listFiles()) {
            int termCode = parseTermCode(pdfPath.getName());
            result.put(termCode, pdfPath.getAbsolutePath());
        }
        return result;
    }

    private static int parseTermCode(String fileName) {
        // figure out term code according to https://data.wisc.edu/infoaccess/available-data-views/uw-madison-student-administration/enrolled-student/stdnt-term-codes/
        // The term code is derived as follows:
        // Character 1 = Century (0 = 1900 and 1 = 2000)
        // Character 2 & 3 = Academic Year
        // Character 4 = Term ( 2=Fall, 4=Spring and 6=Summer)

        StringBuilder s = new StringBuilder(4);
        String splitName = (fileName.split("-")[2] += fileName.split("-")[3])
            .replace(".pdf", "");

        // get first number in term code
        if (splitName.charAt(0) == '2') {
            s.append('1');
        }
        else if (splitName.charAt(0) == '1') {
            s.append('0');
        }

        // get last number in term code
        char last = '6';
        if (splitName.contains("fall")) {
            last = '2';
            splitName = splitName.replace("fall", "");
        }
        else if (splitName.contains("spring")) {
            last = '4';
            splitName = splitName.replace("spring", "");
        }

        // get characters 2 & 3
        s.append(splitName.substring(splitName.length() - 2));
        s.append(last);

        return Integer.parseInt(s.toString());
    }
}
