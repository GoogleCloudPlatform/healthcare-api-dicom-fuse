// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.dicomwebfuse.entities.cache;

import com.google.dicomwebfuse.fuse.Command;
import com.google.dicomwebfuse.entities.Instance;
import java.util.concurrent.atomic.AtomicLong;

public class InstanceContent {

  private final Instance instance;
  // offset for macOS for to prevent the following error:
  // https://github.com/osxfuse/osxfuse/issues/587
  private final AtomicLong offset = new AtomicLong();
  private long instanceSize;
  private volatile Command command;

  public InstanceContent(Instance instance) {
    this.instance = instance;
  }

  public Instance getInstance() {
    return instance;
  }

  public long getInstanceSize() {
    return instanceSize;
  }

  public void setInstanceSize(long instanceSize) {
    this.instanceSize = instanceSize;
  }

  public Command getCommand() {
    return command;
  }

  public void setCommand(Command command) {
    this.command = command;
  }

  public AtomicLong getOffset() {
    return offset;
  }
}
