package com.maojianwei.odl.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.maoodldemo.rev150105.MaoHelloWorldInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.maoodldemo.rev150105.MaoHelloWorldOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.maoodldemo.rev150105.MaoHelloWorldOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.maoodldemo.rev150105.MaoOdlDemoService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import java.util.concurrent.Future;

/**
 * Created by mao on 17-12-23.
 */
public class MaoOdlDemoManager implements MaoOdlDemoService {

    @Override
    public Future<RpcResult<MaoHelloWorldOutput>> maoHelloWorld(MaoHelloWorldInput input) {
        MaoHelloWorldOutputBuilder maoHelloWorldOutputBuilder = new MaoHelloWorldOutputBuilder();
        maoHelloWorldOutputBuilder.setGreeting(input.getName() + " contact beijing tower on 118.5");
        return RpcResultBuilder.success(maoHelloWorldOutputBuilder.build()).buildFuture();
    }
}
