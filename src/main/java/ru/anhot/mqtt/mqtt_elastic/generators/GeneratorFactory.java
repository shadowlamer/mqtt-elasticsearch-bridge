package ru.anhot.mqtt.mqtt_elastic.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GeneratorFactory {

    private List<Generator> generators;
    private Generator defaultGenerator;

    public GeneratorFactory(Pattern pattern) {
        this.generators = new ArrayList<>();
        this.generators.add(new IdGenerator());
        this.generators.add(new UuidGenerator());
        this.generators.add(new MatcherGenerator(pattern));

        this.defaultGenerator = new ValueGenerator();
    }

    public List<Generator> getGenerators() {
        return generators;
    }

    public Generator getDefaultGenerator() {
        return defaultGenerator;
    }

    public Generator findSuitableGenerator(String s) {
        return generators.stream().filter(g -> g.matches(s)).findFirst().orElse(defaultGenerator);
    }

}
