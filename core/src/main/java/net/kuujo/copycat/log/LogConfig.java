/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.copycat.log;

import net.kuujo.copycat.Copyable;
import net.kuujo.copycat.internal.util.Services;
import net.kuujo.copycat.spi.RetentionPolicy;

import java.io.File;

/**
 * Log configuration.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class LogConfig implements Copyable<LogConfig> {
  private File directory = new File(System.getProperty("java.io.tmpdir"), "copycat");
  private int segmentSize = 1024 * 1024;
  private long segmentInterval = Long.MAX_VALUE;
  private boolean flushOnWrite = false;
  private long flushInterval = Long.MAX_VALUE;
  private RetentionPolicy retentionPolicy = log -> true;

  public LogConfig() {
  }

  public LogConfig(String resource) {
    Services.apply(resource, this);
  }

  private LogConfig(LogConfig config) {
    this.directory = config.directory;
    this.segmentSize = config.segmentSize;
    this.segmentInterval = config.segmentInterval;
    this.flushOnWrite = config.flushOnWrite;
    this.flushInterval = config.flushInterval;
    this.retentionPolicy = config.retentionPolicy;
  }

  @Override
  public LogConfig copy() {
    return new LogConfig(this);
  }

  /**
   * Sets the log directory.
   *
   * @param directory The log directory.
   */
  public void setDirectory(String directory) {
    this.directory = new File(directory);
  }

  /**
   * Sets the log directory.
   *
   * @param directory The log directory.
   */
  public void setDirectory(File directory) {
    this.directory = directory;
  }

  /**
   * Returns the log directory.
   *
   * @return The log directory.
   */
  public File getDirectory() {
    return directory;
  }

  /**
   * Sets the log directory, returning the log configuration for method chaining.
   *
   * @param directory The log directory.
   * @return The log configuration.
   */
  public LogConfig withDirectory(String directory) {
    this.directory = new File(directory);
    return this;
  }

  /**
   * Sets the log directory, returning the log configuration for method chaining.
   *
   * @param directory The log directory.
   * @return The log configuration.
   */
  public LogConfig withDirectory(File directory) {
    this.directory = directory;
    return this;
  }

  /**
   * Sets the log segment size.
   *
   * @param segmentSize The log segment size.
   */
  public void setSegmentSize(int segmentSize) {
    this.segmentSize = segmentSize;
  }

  /**
   * Returns the log segment size.
   *
   * @return The log segment size.
   */
  public int getSegmentSize() {
    return segmentSize;
  }

  /**
   * Sets the log segment size, returning the log configuration for method chaining.
   *
   * @param segmentSize The log segment size.
   * @return The log configuration.
   */
  public LogConfig withSegmentSize(int segmentSize) {
    this.segmentSize = segmentSize;
    return this;
  }

  /**
   * Sets the log segment interval.
   *
   * @param segmentInterval The log segment interval.
   */
  public void setSegmentInterval(long segmentInterval) {
    this.segmentInterval = segmentInterval;
  }

  /**
   * Returns the log segment interval.
   *
   * @return The log segment interval.
   */
  public long getSegmentInterval() {
    return segmentInterval;
  }

  /**
   * Sets the log segment interval, returning the log configuration for method chaining.
   *
   * @param segmentInterval The log segment interval.
   * @return The log configuration.
   */
  public LogConfig withSegmentInterval(long segmentInterval) {
    this.segmentInterval = segmentInterval;
    return this;
  }

  /**
   * Sets whether to flush the log to disk on every write.
   *
   * @param flushOnWrite Whether to flush the log to disk on every write.
   */
  public void setFlushOnWrite(boolean flushOnWrite) {
    this.flushOnWrite = flushOnWrite;
  }

  /**
   * Returns whether to flush the log to disk on every write.
   *
   * @return Whether to flush the log to disk on every write.
   */
  public boolean isFlushOnWrite() {
    return flushOnWrite;
  }

  /**
   * Sets whether to flush the log to disk on every write, returning the log configuration for method chaining.
   *
   * @param flushOnWrite Whether to flush the log to disk on every write.
   * @return The log configuration.
   */
  public LogConfig withFlushOnWrite(boolean flushOnWrite) {
    this.flushOnWrite = flushOnWrite;
    return this;
  }

  /**
   * Sets the log flush interval.
   *
   * @param flushInterval The log flush interval.
   */
  public void setFlushInterval(long flushInterval) {
    this.flushInterval = flushInterval;
  }

  /**
   * Returns the log flush interval.
   *
   * @return The log flush interval.
   */
  public long getFlushInterval() {
    return flushInterval;
  }

  /**
   * Sets the log flush interval, returning the log configuration for method chaining.
   *
   * @param flushInterval The log flush interval.
   * @return The log configuration.
   */
  public LogConfig withFlushInterval(long flushInterval) {
    this.flushInterval = flushInterval;
    return this;
  }

  /**
   * Sets the log retention policy.
   *
   * @param retentionPolicy The log retention policy.
   */
  public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
    this.retentionPolicy = retentionPolicy;
  }

  /**
   * Returns the log retention policy.
   *
   * @return The log retention policy.
   */
  public RetentionPolicy getRetentionPolicy() {
    return retentionPolicy;
  }

  /**
   * Sets the log retention policy, returning the log configuration for method chaining.
   *
   * @param retentionPolicy The log retention policy.
   * @return The log configuration.
   */
  public LogConfig withRetentionPolicy(RetentionPolicy retentionPolicy) {
    this.retentionPolicy = retentionPolicy;
    return this;
  }

}