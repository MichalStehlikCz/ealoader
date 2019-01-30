package com.provys.ealoader;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
class RunEALoad {

    private String provysAddress;
    private String eaAddress;
    private String kerPwd;

    RunEALoad setProvysAddress(String provysAddress) {
        this.provysAddress = provysAddress;
        return this;
    }

    RunEALoad setEAAddress(String eaAddress) {
        this.eaAddress = eaAddress;
        return this;
    }

    RunEALoad setKerPwd(String kerPwd) {
        this.kerPwd = kerPwd;
        return this;
    }

    void run() {

    }
}
