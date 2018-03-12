/*
 * Copyright © 2017 maojianwei and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.maojianwei.odl.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.maoodldemo.rev150105.BeijingTower;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.maoodldemo.rev150105.BeijingTowerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.maoodldemo.rev150105.MaoOdlDemoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.maoodldemo.rev150105.ScheduledBandwidth;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class MaoOdlDemoProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MaoOdlDemoProvider.class);


    private final DataBroker dataBroker;
    private DataTreeChangeListener maoSouthTopologyDataChangeListener;
    private DataTreeChangeListener maoNorthDataChangeListener;
    ListenerRegistration southListenerRegistration;
    ListenerRegistration northListenerRegistration;



    private final RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<MaoOdlDemoService> rpcRegistration;
    private MaoOdlDemoService maoOdlDemoService;


    private final NotificationService notificationService;
    private MaoPacketInListener maoPacketInListener;
    private ListenerRegistration maoPacketInListenerRegistration;


    private final PacketProcessingService packetProcessingService;



    public MaoOdlDemoProvider(final DataBroker dataBroker,
                              final RpcProviderRegistry rpcProviderRegistry,
                              final NotificationService notificationService,
                              final PacketProcessingService packetProcessingService) {
        this.dataBroker = dataBroker;
        LOG.info("Mao - service injected - " + dataBroker.toString());

        this.rpcProviderRegistry = rpcProviderRegistry;
        LOG.info("Mao - service injected - " + rpcProviderRegistry.toString());

        this.notificationService = notificationService;
        LOG.info("Mao - service injected - " + notificationService.toString());

        this.packetProcessingService = packetProcessingService;
        LOG.info("Mao - service injected - " + packetProcessingService.toString());
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {

        // Register my own RPC service

        maoOdlDemoService = new MaoOdlDemoManager();
        rpcRegistration = rpcProviderRegistry.addRpcImplementation(MaoOdlDemoService.class, maoOdlDemoService);



        // Listen to Packet-In notification

        maoPacketInListener = new MaoPacketInListener(packetProcessingService);
        maoPacketInListenerRegistration = notificationService.registerNotificationListener(maoPacketInListener);



        // Listen to YANG DataStore

        this.maoSouthTopologyDataChangeListener = new TopologyDataChangeListener();
        this.maoNorthDataChangeListener = new MaoDataChangeListener();


        InstanceIdentifier southYangId = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(TopologyId.getDefaultInstance("flow:1")))
                .child(Node.class, new NodeKey(NodeId.getDefaultInstance("openflow:2")))
                .build();

        southListenerRegistration = this.dataBroker.registerDataTreeChangeListener(
                new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, southYangId),
                maoSouthTopologyDataChangeListener);



        InstanceIdentifier northYangId = InstanceIdentifier.builder(ScheduledBandwidth.class).build();

        northListenerRegistration = this.dataBroker.registerDataTreeChangeListener(
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, northYangId),
                maoNorthDataChangeListener);



        LOG.info("MaoOdlDemoProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {

        maoPacketInListenerRegistration.close();

        southListenerRegistration.close();

        northListenerRegistration.close();

        rpcRegistration.close();

        LOG.info("MaoOdlDemoProvider Closed");
    }




    private class MaoDataChangeListener implements DataTreeChangeListener {

        @Override
        public void onDataTreeChanged(@Nonnull Collection collection) {
            collection.forEach(c -> LOG.info("Mao - MaoDataChange \n" + c.toString()));
        }
    }

    private class TopologyDataChangeListener implements DataTreeChangeListener {

        @Override
        public void onDataTreeChanged(@Nonnull Collection collection) {
//            collection.forEach(c -> LOG.info("Mao - TopologyDataChange \n" + c.toString()));

            try {


                ReadWriteTransaction readWriteTransaction = dataBroker.newReadWriteTransaction();
                InstanceIdentifier instanceIdentifier1 = InstanceIdentifier.builder(NetworkTopology.class)
                        .child(Topology.class, new TopologyKey(TopologyId.getDefaultInstance("flow:1")))
                        .child(Node.class, new NodeKey(NodeId.getDefaultInstance("openflow:1")))
                        .augmentation(BeijingTower.class)
                        .build();

                readWriteTransaction.merge(LogicalDatastoreType.OPERATIONAL, instanceIdentifier1,
                        new BeijingTowerBuilder().setBeijingFrequency(5511).build());

                CheckedFuture future = readWriteTransaction.submit();

                Futures.addCallback(future, new FutureCallback<Optional<DataObject>>() {
                    @Override
                    public void onSuccess(@Nullable Optional<DataObject> dataObjectOptional) {
                        LOG.info("Mao - write topo OK \n\n");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        LOG.info("Mao - write topo failed \n\n" + throwable.getMessage());
                    }
                });

            } catch (Exception e){
                LOG.warn("Mao - error write \n\n" + e.getMessage());
            }

            try {
                // read from MDSAL
                ReadOnlyTransaction readTransaction = dataBroker.newReadOnlyTransaction();

                InstanceIdentifier instanceIdentifier = InstanceIdentifier.builder(NetworkTopology.class)
                        .child(Topology.class, new TopologyKey(TopologyId.getDefaultInstance("flow:1")))
                        .child(Node.class, new NodeKey(NodeId.getDefaultInstance("openflow:1")))
                        .build();
                CheckedFuture result = readTransaction.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier);

                LOG.info("Mao - to read... \n\n");

                Futures.addCallback(result, new FutureCallback<Optional<DataObject>>() {
                    @Override
                    public void onSuccess(@Nullable Optional<DataObject> dataObject) {

                        if (dataObject.isPresent()) {
                            Node node = (Node) dataObject.get();
                            LOG.info("Mao - read topo OK - NodeKey \n\n" + node.getKey());
                            node.getTerminationPoint().forEach(tp ->
                                    LOG.info("Mao - read topo OK - TP \n\n" + tp.getTpId().getValue()));

                            readTransaction.close();

                        } else {
                            readTransaction.close();
                            LOG.info("Mao - read topo ok, but nothing \n\n");
                        }

                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        readTransaction.close();
                        LOG.info("Mao - read topo failed \n\n" + throwable.getMessage());
                    }
                });

            } catch (Exception e){
                LOG.warn("Mao - error read \n\n" + e.getMessage());
            }
        }
    }
}