package properties;

import Interface.SampleController;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Props {

    private static Logger logger;
    static {
        try (FileInputStream ins = new FileInputStream(Paths.get("").toAbsolutePath().toString() + "/logger.config")) {
            LogManager.getLogManager().readConfiguration(ins);
            logger = Logger.getLogger("LoggerKiosk");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static SampleController sampleController;

    private static final String PATH_HOME = Paths.get("").toAbsolutePath().toString();
    private static final String PATH_FACT = "/home/temp/kiosk/";

    //Переменные настроек программы
    private static String Version = "alfa0.1"; //текущая версия программы. Используется для обновления
    private static String wsLogin = ""; //логин на веб сервисе
    private static String wsPassword = ""; //пароль на вебсервисе
    private static String serverAddress = ""; //адрес веб сервера 1с
    private static String webAddressFiles = ""; //адрес веб сервера с опубликованным фалами ресурсов
    private static String soapEndpointUrl = ""; //адрес веб сервиса
    private static String soapAction = ""; //адрес веб сервиса
    private static String kioskHtm = "/Kiosk.htm"; //путь к опубликованному файлу киоска (отображает меню)
    private static String programName = "kiosk.jar"; //имя файла программы

    private static String IPAddress = null;
    private static String MACAddress = null;
    private static boolean registration = false;
    private static boolean connectToServer = false;
    private static boolean errorPrinter = false;
    private static String MASK_ADDR = "10.1";

    public static String getProgramName() {
        return programName;
    }

    public static String getPathFact() {
        return PATH_FACT;
    }

    public static void setRezervAddress() {
        String rezerv = getRezervAddress();

        if (rezerv != null)
            serverAddress = rezerv;
    }

    public static String getWebAddressFiles() {
        return webAddressFiles;
    }

    private static String getRezervAddress() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(getPathHome() + "/rezervAddress.conf")));
            return reader.readLine();
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Не найден файл резервного адреса.", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка при чтении файла резервного адреса", e);
        }
        return null;
    }

    public static void saveRezervAddress() {
        try {
            /*FileWriter fileWriter = new FileWriter(new File(getPathHome() + "/rezervAddress.conf"));

            fileWriter.write();*/
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(getPathHome() + "/rezervAddress.conf")));
            bos.write(serverAddress.getBytes());
            bos.close();
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Не найден файл резервного адреса.", e);
        }catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка при работе с файлом резервного адреса", e);
        }

    }

    public static Logger getLogger() {
        return logger;
    }

    public static String getPathHome() {
        return PATH_HOME;
    }

    private static void getAddress() {
        String IP = "";
        String MAC = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    IP = address.getHostAddress();
                    if (IP.contains(MASK_ADDR)) {
                        byte[] bmac = networkInterface.getHardwareAddress();
                        if(bmac != null) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < bmac.length; i++) {
                                sb.append(String.format("%02X%s", bmac[i], (i < bmac.length - 1) ? "-" : ""));
                            }

                            MAC = sb.toString();

                            break;
                        }
                    }
                }
                if (!MAC.equals("")) {
                    break;
                }
            }

            IPAddress = IP;
            MACAddress = MAC;
        } catch (SocketException e) {
            Props.getLogger().log(Level.WARNING, "Не удалось получить локальный адрес.", e);
        }
    }

    public static String getIPAddress() {
        if (IPAddress == null)
            getAddress();
        return IPAddress;
    }

    public static String getMACAddress() {
        if (MACAddress == null)
            getAddress();
        return MACAddress;
    }

    public static String getWsLogin() {
        return wsLogin;
    }

    public static String getWsPassword() {
        return wsPassword;
    }

    public static String getServerAddress() {
        return serverAddress;
    }

    public static String getSoapEndpointUrl() {
        return serverAddress + soapEndpointUrl;
    }

    public static String getSoapAction() {
        return serverAddress + soapAction;
    }

    public static String getKioskHtm() {
        return webAddressFiles + kioskHtm;
    }

    public static SampleController getSampleController() {
        return sampleController;
    }

    public static void setSampleController(SampleController sampleController) {
        Props.sampleController = sampleController;
    }

    public static String getVersion() {
        return Version;
    }

    public static boolean isRegistration() {
        return registration;
    }

    public static void setRegistration(boolean registration) {
        Props.registration = registration;
    }

    public static boolean isConnectToServer() {
        return connectToServer;
    }

    public static void setConnectToServer(boolean connectToServer) {
        Props.connectToServer = connectToServer;
    }

    public static boolean isErrorPrinter() {
        return errorPrinter;
    }

    public static void setErrorPrinter(boolean errorPrinter) {
        Props.errorPrinter = errorPrinter;
    }

}


