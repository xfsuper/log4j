/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.helpers.Constants;
import org.apache.log4j.util.*;


/**
   A superficial but general test of log4j.
 */
public class MinimumTest extends TestCase {
  static String FILTERED = "output/filtered";
  static String EXCEPTION1 = "java.lang.Exception: Just testing";
  static String EXCEPTION2 = "\\s*at .*\\(.*:\\d{1,4}\\)";
  static String EXCEPTION3 = "\\s*at .*\\(Native Method\\)";

  //18 fevr. 2002 20:02:41,551 [main] FATAL ERR - Message 0
  static String TTCC_PAT =
    Filter.ABSOLUTE_DATE_AND_TIME_PAT
    + " \\[main]\\ (DEBUG|INFO|WARN|ERROR|FATAL) .* - Message \\d{1,2}";
  static String TTCC2_PAT =
    Filter.ABSOLUTE_DATE_AND_TIME_PAT
    + " \\[main]\\ (DEBUG|INFO|WARN|ERROR|FATAL) .* - Messages should bear numbers 0 through 23\\.";

  //18 fvr. 2002 19:49:53,456
  Logger root;
  Logger logger;

  public MinimumTest(String name) {
    super(name);
  }

  public void setUp() {
    root = Logger.getRootLogger();
    root.removeAllAppenders();
  }

  public void tearDown() {
    root.getLoggerRepository().resetConfiguration();
  }

  public void simple() throws Exception {
    Layout layout = new SimpleLayout();
    Appender appender = new FileAppender(layout, "output/simple", false);
    root.addAppender(appender);
    common();

    Transformer.transform(
      "output/simple", FILTERED,
      new Filter[] { new LineNumberFilter(), new SunReflectFilter(), 
                     new JunitTestRunnerFilter() });

    assertTrue(Compare.compare(FILTERED, "witness/simple"));
  }

  /**
   * Tests deprecated TTCCLayout.
   *
   * @deprecated Class under test is deprecated.
   * @throws Exception if io exception.
   */
  public void ttcc() throws Exception {
    TTCCLayout layout = new TTCCLayout();
    layout.setDateFormat(Constants.DATE_AND_TIME_FORMAT);
    layout.activateOptions();
    
    Appender appender = new FileAppender(layout, "output/ttcc", false);
    root.addAppender(appender);
    common();

    ControlFilter cf1 =
      new ControlFilter(
        new String[] { TTCC_PAT, TTCC2_PAT, EXCEPTION1, EXCEPTION2, EXCEPTION3 });

    Transformer.transform(
      "output/ttcc", FILTERED,
      new Filter[] {
        cf1, new LineNumberFilter(), new AbsoluteDateAndTimeFilter(),
        new SunReflectFilter(), new JunitTestRunnerFilter()
      });

    assertTrue(Compare.compare(FILTERED, "witness/ttcc"));
  }

  void common() {
    int i = 0;

    // In the lines below, the category names are chosen as an aid in
    // remembering their level values. In general, the category names
    // have no bearing to level values.
    Logger ERR = Logger.getLogger("ERR");
    ERR.setLevel(Level.ERROR);

    Logger INF = Logger.getLogger("INF");
    INF.setLevel(Level.INFO);

    Logger INF_ERR = Logger.getLogger("INF.ERR");
    INF_ERR.setLevel(Level.ERROR);

    Logger DEB = Logger.getLogger("DEB");
    DEB.setLevel(Level.DEBUG);

    // Note: categories with undefined level 
    Logger INF_UNDEF = Logger.getLogger("INF.UNDEF");
    Logger INF_ERR_UNDEF = Logger.getLogger("INF.ERR.UNDEF");
    Logger UNDEF = Logger.getLogger("UNDEF");

    // These should all log.----------------------------
    ERR.log(Level.FATAL, "Message " + i);
    i++; //0
    ERR.error("Message " + i);
    i++;

    INF.log(Level.FATAL, "Message " + i);
    i++; // 2
    INF.error("Message " + i);
    i++;
    INF.warn("Message " + i);
    i++;
    INF.info("Message " + i);
    i++;

    INF_UNDEF.log(Level.FATAL, "Message " + i);
    i++; //6
    INF_UNDEF.error("Message " + i);
    i++;
    INF_UNDEF.warn("Message " + i);
    i++;
    INF_UNDEF.info("Message " + i);
    i++;

    INF_ERR.log(Level.FATAL, "Message " + i);
    i++; // 10
    INF_ERR.error("Message " + i);
    i++;

    INF_ERR_UNDEF.log(Level.FATAL, "Message " + i);
    i++;
    INF_ERR_UNDEF.error("Message " + i);
    i++;

    DEB.log(Level.FATAL, "Message " + i);
    i++; //14
    DEB.error("Message " + i);
    i++;
    DEB.warn("Message " + i);
    i++;
    DEB.info("Message " + i);
    i++;
    DEB.debug("Message " + i);
    i++;

    // defaultLevel=DEBUG
    UNDEF.log(Level.FATAL, "Message " + i);
    i++; // 19
    UNDEF.error("Message " + i);
    i++;
    UNDEF.warn("Message " + i);
    i++;
    UNDEF.info("Message " + i);
    i++;
    UNDEF.debug("Message " + i, new Exception("Just testing."));
    i++;

    // -------------------------------------------------
    // The following should not log
    ERR.warn("Message " + i);
    i++;
    ERR.info("Message " + i);
    i++;
    ERR.debug("Message " + i);
    i++;

    INF.debug("Message " + i);
    i++;
    INF_UNDEF.debug("Message " + i);
    i++;

    INF_ERR.warn("Message " + i);
    i++;
    INF_ERR.info("Message " + i);
    i++;
    INF_ERR.debug("Message " + i);
    i++;
    INF_ERR_UNDEF.warn("Message " + i);
    i++;
    INF_ERR_UNDEF.info("Message " + i);
    i++;
    INF_ERR_UNDEF.debug("Message " + i);
    i++;

    // -------------------------------------------------
    INF.info("Messages should bear numbers 0 through 23.");
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new MinimumTest("simple"));
    suite.addTest(new MinimumTest("ttcc"));

    return suite;
  }
}
