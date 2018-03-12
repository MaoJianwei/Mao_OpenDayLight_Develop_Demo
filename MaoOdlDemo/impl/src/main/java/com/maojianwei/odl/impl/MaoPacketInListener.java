package com.maojianwei.odl.impl;

import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mao on 17-12-26.
 */
public class MaoPacketInListener implements PacketProcessingListener {

    private static final Logger LOG = LoggerFactory.getLogger(MaoOdlDemoProvider.class);


    private final PacketProcessingService packetProcessingService;

    public MaoPacketInListener(PacketProcessingService packetProcessingService) {
        this.packetProcessingService = packetProcessingService;
    }


//    private NotificationPublishService notificationPublishService;
//
//
//    public MaoPacketInListener(NotificationPublishService notificationPublishService) {
//        this.notificationPublishService = notificationPublishService;
//    }


    @Override
    public void onPacketReceived(PacketReceived packetReceived) {
        if (packetReceived != null) {
            LOG.info("Mao - packet received \n\n" + packetReceived.toString());


//            Action
//
//            new ActionBuilder().setKey(new ActionKey)
//
//            new TransmitPacketInputBuilder()
//                    .setNode(packetReceived.)
//                    .setIngress()
//                    .setEgress()
//                    .setAction()
//                    .setPayload(packetReceived.getPayload())
//                    .build();
//
//            packetProcessingService.transmitPacket()


        } else {
            LOG.warn("Mao - packet received, but null !!! \n\n");
        }
    }

}
