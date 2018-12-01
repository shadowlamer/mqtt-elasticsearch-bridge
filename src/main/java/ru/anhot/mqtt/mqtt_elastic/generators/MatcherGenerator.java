package ru.anhot.mqtt.mqtt_elastic.generators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherGenerator implements Generator {
    private static final String GROUP_PATTERN = "\\$(\\d)";
    private Pattern groupPattern;
    private Pattern pattern;


    public MatcherGenerator(Pattern pattern) {
        this.groupPattern = Pattern.compile(GROUP_PATTERN);
        this.pattern = pattern;

    }

    @Override
    public boolean matches(String s) {
        Matcher groupMatcher = groupPattern.matcher(s);
        return groupMatcher.matches();
    }

    @Override
    public Object getValue(String topic, String from, Object value) {
        Matcher matcher = pattern.matcher(topic);
        if (matcher.matches()) {
            Matcher groupMatcher = groupPattern.matcher(from);
            if (groupMatcher.matches()) {
                Integer group = Integer.valueOf(groupMatcher.group(1));
                return matcher.group(group);
            }
        }
        return "";
    }

}
