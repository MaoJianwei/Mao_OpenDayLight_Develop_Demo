/*
 * Copyright © 2017 maojianwei and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.maojianwei.odl.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.maojianwei.odl.cli.api.MaoOdlDemoCliCommands;

public class MaoOdlDemoCliCommandsImpl implements MaoOdlDemoCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(MaoOdlDemoCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public MaoOdlDemoCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("MaoOdlDemoCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}