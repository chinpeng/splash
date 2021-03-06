/*
 * Copyright (C) 2018 MemVerge Inc.
 *
 * Add config items for Splash.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.spark.shuffle

import org.apache.spark.internal.config.{ConfigBuilder, ConfigEntry}
import org.apache.spark.network.util.ByteUnit

object SplashOpts {
  lazy val storageFactoryName: ConfigEntry[String] =
    ConfigBuilder("spark.shuffle.splash.storageFactory")
        .doc("class name of the storage factory to use.")
        .stringConf
        .createWithDefault("com.memverge.splash.shared.SharedFSFactory")

  lazy val localSplashFolder: ConfigEntry[String] =
    ConfigBuilder("spark.shuffle.splash.folder")
        .doc("location of the local folder")
        .stringConf
        .createWithDefault(null)

  lazy val clearShuffleOutput: ConfigEntry[Boolean] =
    ConfigBuilder("spark.shuffle.splash.clearShuffleOutput")
        .doc("clear shuffle output if set to true.")
        .booleanConf
        .createWithDefault(true)

  // spark options
  lazy val forceSpillElements: ConfigEntry[Int] =
    createIfNotExists("spark.shuffle.spill.numElementsForceSpillThreshold", builder => {
      builder.doc("The maximum number of elements in memory before forcing the shuffle sorter to spill. " +
          "By default it's Integer.MAX_VALUE, which means we never force the sorter to spill, " +
          "until we reach some limitations, like the max page size limitation for the pointer " +
          "array in the sorter.")
          .intConf
          .createWithDefault(Integer.MAX_VALUE)
    })

  lazy val useRadixSort: ConfigEntry[Boolean] =
    createIfNotExists("spark.shuffle.sort.useRadixSort", builder => {
      builder.booleanConf.createWithDefault(true)
    })

  lazy val fastMergeEnabled: ConfigEntry[Boolean] =
    createIfNotExists("spark.shuffle.unsafe.fastMergeEnabled", builder => {
      builder.booleanConf.createWithDefault(true)
    })

  lazy val shuffleCompress: ConfigEntry[Boolean] =
    createIfNotExists("spark.shuffle.compress", builder => {
      builder.booleanConf.createWithDefault(true)
    })

  lazy val shuffleInitialBufferSize: ConfigEntry[Int] =
    createIfNotExists("spark.shuffle.sort.initialBufferSize", builder => {
      builder
          .doc("Shuffle initial buffer size used by the sorter.")
          .intConf
          .createWithDefault(4096)
    })

  lazy val memoryMapThreshold: ConfigEntry[Long] =
    createIfNotExists("spark.storage.memoryMapThreshold", builder => {
      builder.bytesConf(ByteUnit.BYTE).createWithDefaultString("2m")
    })

  // compatible entries for spark 2.1, scala 2.10, migrated from spark 2.3
  lazy val shuffleFileBufferKB: ConfigEntry[Long] =
    createIfNotExists("spark.shuffle.file.buffer", builder => {
    builder.doc("Size of the in-memory buffer for each shuffle file output stream, in KiB unless " +
        "otherwise specified. These buffers reduce the number of disk seeks and system calls " +
        "made in creating intermediate shuffle files.")
        .bytesConf(ByteUnit.KiB)
        .createWithDefaultString("32k")
  })

  lazy val bypassSortThreshold: ConfigEntry[Int] =
    createIfNotExists("spark.shuffle.sort.bypassMergeThreshold", builder => {
      builder.doc("Use bypass merge sort shuffle writer if partition is lower than this")
          .intConf
          .createWithDefault(200)
    })

  private def createIfNotExists[T](
      optionKey: String,
      f: ConfigBuilder => ConfigEntry[T]): ConfigEntry[T] = {
    val existingEntry: ConfigEntry[_] = ConfigEntry.findEntry(optionKey)
    if (existingEntry != null) {
      existingEntry.asInstanceOf[ConfigEntry[T]]
    } else {
      f(ConfigBuilder(optionKey))
    }
  }
}
