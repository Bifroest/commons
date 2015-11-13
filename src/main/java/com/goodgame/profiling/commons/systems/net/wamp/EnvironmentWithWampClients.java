/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodgame.profiling.commons.systems.net.wamp;

import ws.wamp.jawampa.WampClient;

/**
 *
 * @author hkraemer@ggs-hh.net
 */
public interface EnvironmentWithWampClients {
    public WampClient getWampClientForRealm( String realm );
}
