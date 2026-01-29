package com.example.modbusapplication.Service;

import com.example.modbusapplication.Model.SearchCompanyDao;
import java.util.*;

public class SimpleSearchEngine {
    private List<SearchCompanyDao> users;

    public SimpleSearchEngine(List<SearchCompanyDao> users) {
        this.users = users;
    }

    public List<SearchCompanyDao> search(String query) {
        String lowerQuery = query.toLowerCase();
        List<SearchCompanyDao> matched = new ArrayList<>();

        for (SearchCompanyDao user : users) {
            String companyName = user.getCompanyName().toLowerCase();

            // Check if companyName or any word inside it starts with the query
            if (companyName.startsWith(lowerQuery)) {
                matched.add(user);
            } else {
                for (String token : companyName.split("\\W+")) {
                    if (token.startsWith(lowerQuery)) {
                        matched.add(user);
                        break;
                    }
                }
            }
        }
        return matched;
    }
}