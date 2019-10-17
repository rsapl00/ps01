package com.albertsons.app.ps01.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NonNull
@NoArgsConstructor
public class HttpHeaderMap {
    private String username;
    private String group;
    private String division;

    public HttpHeaderMap(String username, String group, String division) {
        this.username = username;
        this.group = group;
        this.division = division;
    }

    public Boolean isValid() {
        if ((username == null && username.isEmpty()) || (group == null && group.isEmpty())
                || (division == null && division.isEmpty())) {
            return false;
        }

        return true;
    }
}