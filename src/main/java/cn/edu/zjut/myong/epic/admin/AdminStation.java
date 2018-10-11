/*
 * Copyright 2017 Yong Min
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.edu.zjut.myong.epic.admin;

import cn.edu.zjut.myong.epic.Configuration;
import cn.edu.zjut.myong.epic.Experiment;
import cn.edu.zjut.myong.epic.Game;
import cn.edu.zjut.myong.epic.db.BerkeleyDBGeneral;
import cn.edu.zjut.myong.epic.model.Harvest;
import cn.edu.zjut.myong.epic.model.ICConfiguration;
import cn.edu.zjut.myong.epic.model.ServletHarvest;
import cn.edu.zjut.myong.epic.server.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class AdminStation {

    public static final int INITIAL = 0;
    public static final int SIGNED = 1;
    public static final int ESTABLISHED = 2;

    private static String signingName = "";
    private static String signingPassword = "";
    private static int expPort = 1209;

    public static String adminCode = null;
    private static Server expServer = null;
    private static HandlerCollection handlerCollection = new HandlerCollection(true);
    private static ServletContextHandler playerHandle;
    public static int expStatus = INITIAL;
    private static Map<String, String> expImages = new HashMap<>();

    static {
        try {
            File imgDir = new File("./web/images/uploading");
            File[] imgs = imgDir.listFiles();
            if (imgs != null) {
                for (File img : imgs) {
                    String ename = img.getName().toLowerCase();
                    if (ename.endsWith("jpg") || ename.endsWith("png") || ename.endsWith("bmp")) {
                        expImages.put(img.getName(), "/images/uploading/" + img.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getExpPort() {
        return expPort;
    }

    public static int init(String code) {
        if (adminCode == null || code == null || !adminCode.equals(code.trim())) {
            return INITIAL;
        } else {
            return expStatus;
        }
    }

    public static boolean startServer() {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("admin.properties"));
            signingName = prop.getProperty("name").trim();
            signingPassword = prop.getProperty("password").trim();
            expPort = Integer.parseInt(prop.getProperty("port").trim());
            return startServer(expPort);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean startServer(int port) {
        try {
            /*
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream
                    ("./configuration.json")));
            String line = "";
            String json = "";
            while ((line = reader.readLine()) != null) {
                json = json + line;
            }
            reader.close();
            ICConfiguration icc = new ICConfiguration(json.trim());
            Experiment.init(icc);
            expStatus = ESTABLISHED;
            */

            /**
             * Create a basic Jetty server object that will listen on port 8080. Note that if you set this to port 0, then a randomly available port will be assigned that you can either look in the logs for the port, or programmatically obtain it for use in test cases.
             */
            expServer = new org.eclipse.jetty.server.Server(port);

            /**
             * Create the ResourceHandler. It is the object that will actually handle the request for a given file. It is a Jetty Handler object so it is suitable for chaining with other handlers as you will see in other examples.
             */
            ResourceHandler resourceHandler = new ResourceHandler();
            /**
             * Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of. In this example it is the current directory but it can be configured to anything that the jvm has access to.
             */
            resourceHandler.setDirectoriesListed(true);
            resourceHandler.setResourceBase("./web");

            /**
             * Create the ServletHandler.
             */
            ServletContextHandler adminServletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            adminServletHandler.setContextPath("/");
            adminServletHandler.addServlet(new ServletHolder(new ServletAdminInit()), "/servlet/admin/init");
            adminServletHandler.addServlet(new ServletHolder(new ServletAdminSignin()), "/servlet/admin/signin");
            adminServletHandler.addServlet(new ServletHolder(new ServletAdminUploadImages()), "/servlet/admin/img_upload");
            adminServletHandler.addServlet(new ServletHolder(new ServletAdminListImages()), "/servlet/admin/img_list");
            adminServletHandler.addServlet(new ServletHolder(new ServletAdminDeleteImages()), "/servlet/admin/img_delete");
            adminServletHandler.addServlet(new ServletHolder(new ServletAdminICMonitor()), "/servlet/admin/ic/monitor");
            adminServletHandler.addServlet(new ServletHolder(new ServletAdminICStart()), "/servlet/admin/ic/start");
            adminServletHandler.addServlet(new ServletHolder(new ServletAdminICReStart()), "/servlet/admin/ic/restart");

            adminServletHandler.addServlet(new ServletHolder(new ServletListExperiments()), "/servlet/admin/list_db");
            adminServletHandler.addServlet(new ServletHolder(new ServletListGames()), "/servlet/admin/list_gm");
            adminServletHandler.addServlet(new ServletHolder(new ServletDownloadGame()), "/servlet/admin/down_gm");

            adminServletHandler.addServlet(new ServletHolder(new ServletTesting()), "/servlet/testing");
            adminServletHandler.addServlet(new ServletHolder(new ServletInitialization()), "/servlet/init");
            adminServletHandler.addServlet(new ServletHolder(new ServletSignin()), "/servlet/signin");
            adminServletHandler.addServlet(new ServletHolder(new ServletWaiting4Main()), "/servlet/waiting4main");
            //adminServletHandler.addServlet(new ServletHolder(new ServletWaiting4Instruction()), "/servlet/waiting4instruction");
            //adminServletHandler.addServlet(new ServletHolder(new ServletWaiting4Survey()), "/servlet/waiting4survey");
            //adminServletHandler.addServlet(new ServletHolder(new ServletWaiting4Communication()), "/servlet/waiting4communication");
            //adminServletHandler.addServlet(new ServletHolder(new ServletWaiting4Decision()), "/servlet/waiting4decision");
            //adminServletHandler.addServlet(new ServletHolder(new ServletWaiting4Redistribution()), "/servlet/waiting4redistribution");
            //adminServletHandler.addServlet(new ServletHolder(new ServletWaiting4Process()), "/servlet/waiting4process");
            //adminServletHandler.addServlet(new ServletHolder(new ServletWaiting4Next()), "/servlet/next");
            adminServletHandler.addServlet(new ServletHolder(new ServletChat()), "/servlet/chat");
            // adminServletHandler.addServlet(new ServletHolder(new ServletUploadQuestionnaire()),
            // "/servlet/upload_questionnaire");
            adminServletHandler.addServlet(new ServletHolder(new ServletFinished()), "/servlet/finish");

            adminServletHandler.addServlet(new ServletHolder(new ServletHarvest()), "/servlet/harvest");

            // Add the ResourceHandler to the server.
            handlerCollection.addHandler(resourceHandler);
            handlerCollection.addHandler(adminServletHandler);
            expServer.setHandler(handlerCollection);

            /**
             * Start things up! By using the server.join() the server thread will join with the current thread. See "http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Thread.html#join()" for more details.
             */
            expServer.start();
            expServer.join();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean startExp(String iCode, Configuration config) {
        if (adminCode == null || iCode == null || !adminCode.equals(iCode)) {
            return false;
        }

        try {
            /**
             * Initializing the experiment.
             */
            Experiment.init(config);
            Harvest.init(config.getGroups().keySet());
            expStatus = ESTABLISHED;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //从servlet得到用户名和密码进行校验的一个方法
    public static String signin(String name, String password) {
        if (name.equals(signingName) && password.equals(signingPassword)) {
            adminCode = "" + new Random(new Date().getTime()).nextLong();
            expStatus = SIGNED;
            return adminCode;
        } else {
            return null;
        }
    }

    public static boolean restartExp(String iCode) {
        // handlerCollection.removeHandler(playerHandle);
        if (adminCode == null || iCode == null || !adminCode.equals(iCode)) {
            return false;
        }

        Experiment.allPlayers = null;
        Experiment.conf = null;
        Experiment.eid = "";
        Experiment.gameProperties = null;
        Experiment.groups = null;
        Experiment.model = null;
        Experiment.questions = null;
        Experiment.slides = null;
        Experiment.surveyThreshold = 0;
        Experiment.surveyTotalScores = 0;
        Experiment.version = 0;
        if (Experiment.db != null) {
            Experiment.db.closeConnection();
            Experiment.db = null;
        }
        adminCode = "";
        expStatus = SIGNED;
        ICConfiguration.clearClasses();
        return true;
    }

    public static Map<String, String> getExpImages() {
        return expImages;
    }

    public static void uploadImage(String file, String url) {
        expImages.put(file, url);
    }

    public static void removeImage(String file) {
        try {
            File f = new File("./web/images/uploading/" + file);
            if (f.exists() && f.delete()) {
                expImages.remove(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> listExperiments() {
        BerkeleyDBGeneral<Game> berkeley;
        try {
            if (Experiment.db == null) {
                berkeley = new BerkeleyDBGeneral();
                berkeley.openEnvironment("./db");
            } else {
                berkeley = (BerkeleyDBGeneral<Game>) Experiment.db;
            }

            List<String> dbs = berkeley.env.getDatabaseNames();
            if (Experiment.db == null) {
                berkeley.closeConnection();
            }

            return dbs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<String> listGames(String db) {
        BerkeleyDBGeneral<Game> berkeley;
        try {
            if (Experiment.db == null) {
                berkeley = new BerkeleyDBGeneral();
                berkeley.openConnection("./db", db);
            } else {
                berkeley = (BerkeleyDBGeneral<Game>) Experiment.db;
            }

            List<String> games = berkeley.getAllKeys();
            if (Experiment.db == null) {
                berkeley.closeConnection();
            }

            return games;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static Game getGame(String db, String game) {
        BerkeleyDBGeneral<Game> berkeley;
        try {
            if (Experiment.db == null) {
                berkeley = new BerkeleyDBGeneral();
                berkeley.openConnection("./db", db);
            } else {
                berkeley = (BerkeleyDBGeneral<Game>) Experiment.db;
            }

            Game dg = berkeley.get(game);
            if (Experiment.db == null) {
                berkeley.closeConnection();
            }

            return dg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
