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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class BuildVersionSingleton {

    private static final String BUILD_VERSION_PROPFILE = "repoversion";
    private static final String BUILD_VERSION_PROPNAME = "version";


    private static final BuildVersionSingleton INSTANCE = new BuildVersionSingleton();

    public static String getBuildVersion() {
        return INSTANCE.buildVersion;
    }
    private final String buildVersion;

    private BuildVersionSingleton() {
        String bv = "n/a";

        try {

            bv = ResourceBundle.getBundle(BUILD_VERSION_PROPFILE).getString(BUILD_VERSION_PROPNAME);

        } catch (MissingResourceException e) {
            // ignore missing resources
        }

        buildVersion = bv;
    }
}
