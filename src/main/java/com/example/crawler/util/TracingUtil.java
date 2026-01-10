package com.example.crawler.util;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TracingUtil {
    
    private final Tracer tracer;
    
    @Autowired
    public TracingUtil(OpenTelemetry openTelemetry) {
        TracerProvider tracerProvider = openTelemetry.getTracerProvider();
        this.tracer = tracerProvider.get("company-crawler");
    }
    
    public Span startSpan(String spanName) {
        return tracer.spanBuilder(spanName).startSpan();
    }
    
    public <T> T trace(String spanName, TraceableOperation<T> operation) {
        Span span = startSpan(spanName);
        try (Scope scope = span.makeCurrent()) {
            return operation.execute();
        } catch (RuntimeException e) {
            span.recordException(e);
            throw e;
        } catch (Exception e) {
            span.recordException(e);
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }
    
    public void trace(String spanName, Runnable operation) {
        Span span = startSpan(spanName);
        try (Scope scope = span.makeCurrent()) {
            operation.run();
        } catch (RuntimeException e) {
            span.recordException(e);
            throw e;
        } catch (Exception e) {
            span.recordException(e);
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }
    
    @FunctionalInterface
    public interface TraceableOperation<T> {
        T execute() throws Exception;
    }
}
