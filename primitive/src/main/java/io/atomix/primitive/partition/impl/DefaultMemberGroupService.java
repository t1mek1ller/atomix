/*
 * Copyright 2018-present Open Networking Foundation
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
package io.atomix.primitive.partition.impl;

import io.atomix.cluster.ClusterEventListener;
import io.atomix.cluster.ClusterService;
import io.atomix.primitive.partition.ManagedMemberGroupService;
import io.atomix.primitive.partition.MemberGroup;
import io.atomix.primitive.partition.MemberGroupEvent;
import io.atomix.primitive.partition.MemberGroupEventListener;
import io.atomix.primitive.partition.MemberGroupProvider;
import io.atomix.primitive.partition.MemberGroupService;
import io.atomix.utils.event.AbstractListenerManager;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default member group service.
 */
public class DefaultMemberGroupService
    extends AbstractListenerManager<MemberGroupEvent, MemberGroupEventListener>
    implements ManagedMemberGroupService {

  private final AtomicBoolean started = new AtomicBoolean();
  private final ClusterService clusterService;
  private final MemberGroupProvider memberGroupProvider;
  private final ClusterEventListener clusterEventListener = event -> recomputeGroups();
  private volatile Collection<MemberGroup> memberGroups;

  public DefaultMemberGroupService(ClusterService clusterService, MemberGroupProvider memberGroupProvider) {
    this.clusterService = clusterService;
    this.memberGroupProvider = memberGroupProvider;
  }

  @Override
  public Collection<MemberGroup> getMemberGroups() {
    return memberGroups;
  }

  private void recomputeGroups() {
    memberGroups = memberGroupProvider.getMemberGroups(clusterService.getNodes());
  }

  @Override
  public CompletableFuture<MemberGroupService> start() {
    if (started.compareAndSet(false, true)) {
      memberGroups = memberGroupProvider.getMemberGroups(clusterService.getNodes());
      clusterService.addListener(clusterEventListener);
    }
    return CompletableFuture.completedFuture(this);
  }

  @Override
  public boolean isRunning() {
    return started.get();
  }

  @Override
  public CompletableFuture<Void> stop() {
    if (started.compareAndSet(true, false)) {
      clusterService.removeListener(clusterEventListener);
    }
    return CompletableFuture.completedFuture(null);
  }
}
