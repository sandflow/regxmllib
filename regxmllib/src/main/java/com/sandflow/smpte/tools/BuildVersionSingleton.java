/*
 * Copyright (c) 2014, Pierre-Anthony Lemieux (pal@sandflow.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.sandflow.smpte.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Retrieves a string that uniquely identifies the current build of the
 * underlying source code of regxmllib. Currently looks for build version at the
 * 'version' property in the /config/repoversion.properties resource.
 */
public class BuildVersionSingleton {

    private static final String BUILD_VERSION_PROPFILE = "/config/repoversion.properties";
    private static final String BUILD_VERSION_PROPNAME = "version";
    private static final String DEFAULT_BUILD_VERSION = "n/a";

    private static final BuildVersionSingleton INSTANCE = new BuildVersionSingleton();

    /**
     * @return Uniquely identifies the current build of the underlying source
     * code of regxmllib
     */
    public static String getBuildVersion() {
        return INSTANCE.buildVersion;
    }
    private String buildVersion = DEFAULT_BUILD_VERSION;

    private BuildVersionSingleton() {

        try {

            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream(BUILD_VERSION_PROPFILE);

            if (in != null) {

                prop.load(in);

                String bv = prop.getProperty(BUILD_VERSION_PROPNAME);

                if (bv != null) {
                    buildVersion = bv;
                }

                in.close();

            }

        } catch (IOException e) {
            // ignore missing resources
        }

    }
}
