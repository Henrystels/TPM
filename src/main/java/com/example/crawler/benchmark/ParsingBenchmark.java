package com.example.crawler.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ParsingBenchmark {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    
    private String htmlContent;
    
    @Setup
    public void setup() {
        // Generate sample HTML content with emails
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        for (int i = 0; i < 1000; i++) {
            sb.append("<p>Contact us at email").append(i).append("@example.com or ");
            sb.append("another").append(i).append("@test.org</p>");
        }
        sb.append("</body></html>");
        htmlContent = sb.toString();
    }
    
    @Benchmark
    public List<String> extractEmailsWithForLoop() {
        List<String> emails = new ArrayList<>();
        Matcher matcher = EMAIL_PATTERN.matcher(htmlContent);
        
        while (matcher.find()) {
            String email = matcher.group().toLowerCase();
            if (!emails.contains(email)) {
                emails.add(email);
            }
        }
        
        return emails;
    }
    
    @Benchmark
    public List<String> extractEmailsWithStream() {
        Matcher matcher = EMAIL_PATTERN.matcher(htmlContent);
        List<String> matches = new ArrayList<>();
        
        while (matcher.find()) {
            matches.add(matcher.group().toLowerCase());
        }
        
        return matches.stream()
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Benchmark
    public List<String> extractEmailsWithParallelStream() {
        Matcher matcher = EMAIL_PATTERN.matcher(htmlContent);
        List<String> matches = new ArrayList<>();
        
        while (matcher.find()) {
            matches.add(matcher.group().toLowerCase());
        }
        
        return matches.parallelStream()
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Benchmark
    public List<String> extractEmailsWithStreamOptimized() {
        return EMAIL_PATTERN.matcher(htmlContent).results()
                .map(matchResult -> matchResult.group().toLowerCase())
                .distinct()
                .collect(Collectors.toList());
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ParsingBenchmark.class.getSimpleName())
                .build();
        
        new Runner(opt).run();
    }
}
