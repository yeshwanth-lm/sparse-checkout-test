/*
 * Copyright (2021) The Delta Lake Project Authors.
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

package io.delta.sharing.spark.util

import java.util.concurrent.TimeUnit

import org.apache.hadoop.conf.Configuration
import org.apache.spark.network.util.JavaUtils
import org.apache.spark.sql.internal.SQLConf

object ConfUtils {

  val NUM_RETRIES_CONF = "spark.delta.sharing.network.numRetries"
  val NUM_RETRIES_DEFAULT = 10

  val MAX_RETRY_DURATION_CONF = "spark.delta.sharing.network.maxRetryDuration"
  val MAX_RETRY_DURATION_DEFAULT_MILLIS = 10L * 60L* 1000L /* 10 mins */

  val TIMEOUT_CONF = "spark.delta.sharing.network.timeout"
  val TIMEOUT_DEFAULT = "320s"

  val MAX_CONNECTION_CONF = "spark.delta.sharing.network.maxConnections"
  val MAX_CONNECTION_DEFAULT = 64

  def numRetries(conf: Configuration): Int = {
    val numRetries = conf.getInt(NUM_RETRIES_CONF, NUM_RETRIES_DEFAULT)
    validateNonNeg(numRetries, NUM_RETRIES_CONF)
    numRetries
  }

  def numRetries(conf: SQLConf): Int = {
    val numRetries = conf.getConfString(NUM_RETRIES_CONF, NUM_RETRIES_DEFAULT.toString).toInt
    validateNonNeg(numRetries, NUM_RETRIES_CONF)
    numRetries
  }

  def maxRetryDurationMillis(conf: Configuration): Long = {
    val maxDur = conf.getLong(MAX_RETRY_DURATION_CONF, MAX_RETRY_DURATION_DEFAULT_MILLIS)
    validateNonNeg(maxDur, MAX_RETRY_DURATION_CONF)
    maxDur
  }

  def maxRetryDurationMillis(conf: SQLConf): Long = {
    val maxDur =
      conf.getConfString(MAX_RETRY_DURATION_CONF, MAX_RETRY_DURATION_DEFAULT_MILLIS.toString).toLong
    validateNonNeg(maxDur, MAX_RETRY_DURATION_CONF)
    maxDur
  }

  def timeoutInSeconds(conf: Configuration): Int = {
    val timeoutStr = conf.get(TIMEOUT_CONF, TIMEOUT_DEFAULT)
    toTimeout(timeoutStr)
  }

  def timeoutInSeconds(conf: SQLConf): Int = {
    val timeoutStr = conf.getConfString(TIMEOUT_CONF, TIMEOUT_DEFAULT)
    toTimeout(timeoutStr)
  }

  def maxConnections(conf: Configuration): Int = {
    val maxConn = conf.getInt(MAX_CONNECTION_CONF, MAX_CONNECTION_DEFAULT)
    validateNonNeg(maxConn, MAX_CONNECTION_CONF)
    maxConn
  }

  private def toTimeout(timeoutStr: String): Int = {
    val timeoutInSeconds = JavaUtils.timeStringAs(timeoutStr, TimeUnit.SECONDS)
    validateNonNeg(timeoutInSeconds, TIMEOUT_CONF)
    if (timeoutInSeconds > Int.MaxValue) {
      throw new IllegalArgumentException(TIMEOUT_CONF + " is too big: " +  timeoutStr)
    }
    timeoutInSeconds.toInt
  }


  private def validateNonNeg(value: Long, conf: String): Unit = {
    if (value < 0L) {
      throw new IllegalArgumentException(conf + " must not be negative")
    }
  }
}
