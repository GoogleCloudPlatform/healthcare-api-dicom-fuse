package com.google.dicomwebfuse.fuse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.dicomwebfuse.entities.cache.CacheTime;
import com.google.dicomwebfuse.log4j2.Log4j2LoggerConfigurator;
import com.google.dicomwebfuse.parser.Arguments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.serce.jnrfuse.ErrorCodes;

class DicomFuseTest {

  private static final String testPath = "/dicomStore/study/series/instance.dcm";
  private static final CacheTime positiveInstanceFilesCacheTime = new CacheTime(60,300);
  private static final CacheTime zeroInstanceFilesCacheTime = new CacheTime(60,0);
  private static final Arguments positiveInstanceFilesCacheTimeArguments = new Arguments();
  private static final Arguments zeroInstanceFilesCacheTimeArguments = new Arguments();

  @BeforeAll
  static void setup() {
    Log4j2LoggerConfigurator log4j2LoggerConfigurator = new Log4j2LoggerConfigurator();
    log4j2LoggerConfigurator.configureSilentLogger();
    positiveInstanceFilesCacheTimeArguments.cacheTime = positiveInstanceFilesCacheTime;
    zeroInstanceFilesCacheTimeArguments.cacheTime = zeroInstanceFilesCacheTime;
  }

  @Test
  void readMethodShouldReturnNegativeResultIfInstanceFilesCacheTimeAboveZero() {
    // given
    Parameters parameters = new Parameters(null, positiveInstanceFilesCacheTimeArguments, null);
    DicomFuse dicomFuse = new DicomFuse(parameters);
    // when
    int readResult = dicomFuse.read(testPath, null, 0, 0, null);
    // then
    assertEquals(-ErrorCodes.EIO(), readResult);
  }

  @Test
  void readMethodShouldReturnZeroResultIfInstanceFilesCacheTimeIsZero() {
    // given
    Parameters parameters = new Parameters(null, zeroInstanceFilesCacheTimeArguments, null);
    DicomFuse dicomFuse = new DicomFuse(parameters);
    // when
    int readResult = dicomFuse.read(testPath, null, 0, 0, null);
    // then
    assertEquals(0, readResult);
  }
}
