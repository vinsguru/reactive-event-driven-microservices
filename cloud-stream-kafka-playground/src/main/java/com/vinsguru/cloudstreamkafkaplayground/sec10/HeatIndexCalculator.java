package com.vinsguru.cloudstreamkafkaplayground.sec10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.util.function.Function;

@Configuration
public class HeatIndexCalculator {

    private static final Logger log = LoggerFactory.getLogger(HeatIndexCalculator.class);

    @Bean
    public Function<Tuple2<Flux<Integer>, Flux<Integer>>, Flux<Long>> processor(){
        return t -> Flux.combineLatest(t.getT1(), t.getT2(), this::findHeatIndex);
        /*
            src1 = 1,2,3,4,5  50ms
            src2 = a,b,c,d    80ms

            Flux.combineLatest(src1, src2, (a, b) -> a + b))

            1a
            2a
            3a
            3b

         */

    }

    private long findHeatIndex(int temperature, int humidity) {
        double c1 = -42.379;
        double c2 = 2.04901523;
        double c3 = 10.14333127;
        double c4 = -0.22475541;
        double c5 = -6.83783 * Math.pow(10, -3);
        double c6 = -5.481717 * Math.pow(10, -2);
        double c7 = 1.22874 * Math.pow(10, -3);
        double c8 = 8.5282 * Math.pow(10, -4);
        double c9 = -1.99 * Math.pow(10, -6);
        double index = c1 + c2 * temperature + c3 * humidity + c4 * temperature * humidity + c5 * Math.pow(temperature, 2) + c6 * Math.pow(humidity, 2) + c7 * Math.pow(temperature, 2) * humidity + c8 * temperature * Math.pow(humidity, 2) + c9 * Math.pow(temperature, 2) * Math.pow(humidity, 2);
        long heatIndex = Math.round(index);
        log.info("temperature: {}, humidity: {}, heatIndex: {}", temperature, humidity, heatIndex);
        return heatIndex;
    }

}
