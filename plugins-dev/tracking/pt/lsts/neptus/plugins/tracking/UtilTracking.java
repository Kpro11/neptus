/*
 * Copyright (c) 2004-2016 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: Pedro Gonçalves
 */

package pt.lsts.neptus.plugins.tracking;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.i18n.I18n;

/** 
 * @author pedrog
 * @version 1.0
 * @category OpenCV-Vision
 *
 */
public class UtilTracking {
    
    private UtilTracking() {
    }
    
    public static String[][] readIpUrl(File nameFile) {
        BufferedReader br = null;
        String lineFile;
        String[] splits;
        String[] emptyData = {"Select Device", "", ""};
        ArrayList<String[]> dataIpCam = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(nameFile));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        dataIpCam.add(emptyData);
        try {
            while ((lineFile = br.readLine()) != null) {
                if(!lineFile.isEmpty()) {
                    splits = lineFile.split("#");                  
                    if(splits.length == 3)
                        dataIpCam.add(splits);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return dataIpCam.toArray(new String[dataIpCam.size()][0]);
    }
    
    public static boolean pingIp(String host) {
        boolean ping = false;
        boolean ping2 = false;
        try {
            String cmd = "";
            if (System.getProperty("os.name").startsWith("Windows")) {
                // For Windows
                cmd = "ping -n 1 " + host;
            }
            else {
                // For Linux and OSX
                cmd = "ping -c 1 " + host;
            }
            Process myProcess = Runtime.getRuntime().exec(cmd);
            try {
                myProcess.waitFor();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            InetAddress hostip = InetAddress.getByName(host);
            ping2 = hostip.isReachable(1000);
            
            if (myProcess.exitValue() == 0 && ping2)
                ping = true;
            else
                ping = false;
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        } // Ping doesn't work
        
        return ping;
    }
    
    public static BufferedImage resizeBufferedImage(BufferedImage img, Size size, boolean divide) {
        if(size != null) {
            BufferedImage dimg = null;
            if (divide)
                dimg = new BufferedImage((int)size.width/2, (int)size.height, img.getType());
            else
                dimg = new BufferedImage((int)size.width, (int)size.height, img.getType());
            
            Graphics2D g2d = dimg.createGraphics();
            
            if (divide)
                g2d.drawImage(img.getScaledInstance((int)size.width/2, (int)size.height, Image.SCALE_SMOOTH), 0, 0, null);
            else
                g2d.drawImage(img.getScaledInstance((int)size.width, (int)size.height, Image.SCALE_SMOOTH), 0, 0, null);
            
            g2d.dispose();
            return dimg;
        }
        else {
            NeptusLog.pub().warn(I18n.text("Size in resizeBufferedImage must be != NULL"));
            return null;
        }
    }

    /**
     * Undistort image.
     * @param image - Distorted image.
     * @param cameraMatrix - Camera matrix.
     * @param distCoeffs - Input vector of distortion coefficients.
     * @return Undistorted image.
     */
    public static Mat undistort(final Mat image, final Mat cameraMatrix, final Mat distCoeffs) {
        final Mat newCameraMtx = Calib3d.getOptimalNewCameraMatrix(cameraMatrix, distCoeffs, image.size(), 0);
        final Mat mat = new Mat();
        Imgproc.undistort(image, mat, cameraMatrix, distCoeffs, newCameraMtx);
        return mat;
    }
}
