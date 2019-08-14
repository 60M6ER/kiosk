package UpdateFromServer;

import Interface.Soap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import properties.Props;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Updater extends Thread {

    public static Updater updater = null;

    @Override
    public void run() {
        while (true) {
            registrationKiosk(true);
        }
    }

    public static void registrationKiosk(boolean thread) {
            if (!thread)
                Props.getLogger().log(Level.INFO, "Начата регистрация.");

            String request = Soap.RegistrationKiosk();

            if (request == null) {
                Props.getLogger().log(Level.WARNING, "Не получен ответ от сервера.");
            } else {
                Props.setConnectToServer(true);
                if (request.equals("update") || request.equals("none")) {
                    updateProgram();
                } else {
                    Gson gson = new GsonBuilder().create();
                    JSONRegistration requestObject = gson.fromJson(request, JSONRegistration.class);

                    if (!requestObject.getLastVersion().equals(Props.getVersion()))
                        updateProgram();

                    if (requestObject.getRegistration().equals("0")) {
                        Props.getLogger().log(Level.INFO, "Не зарегистрирована очередь.");
                    } else {
                        if (!thread) {
                            Props.saveRezervAddress();
                            Props.setRegistration(true);
                            Props.getLogger().log(Level.INFO, "Выполнена регистрация на сервере.");
                        }
                    }
                }
            }

        if (thread) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(30));
            } catch (InterruptedException e) {
                Props.getLogger().log(Level.WARNING, "Ошибка обработчика ожидания", e);
            }
        }

        if (updater == null) {
            updater = new Updater();
            updater.start();
        }
    }

    private static void updateProgram() {
        Props.getLogger().log(Level.INFO, "Обновление программы!");

        if (downloadFileProgram()) {
            Props.getLogger().log(Level.INFO, "Обновление выполнено. Выполняется перезапуск.");
            restartProgram();
        } else {
            Props.getLogger().log(Level.WARNING, "Не удалось обновить программу.");
        }
    }

    public static void restartProgram() {
        try {
            //Process process = Runtime.getRuntime().exec("java -jar " + Props.getPathHome() + "/" + Props.getProgramName());
            Process process = Runtime.getRuntime().exec("reboot");
        } catch (IOException e) {
            Props.getLogger().log(Level.WARNING, "Ошибка при выполнении перезапуска.", e);
        }
    }

    public static boolean downloadFileProgram() {
        try {
            String fileName = Props.getPathFact() + Props.getProgramName();
            URL url = new URL(Props.getWebAddressFiles() + "/" + Props.getProgramName());

            //URL url = new URL(urlStr);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(new File(Props.getPathHome() + "/" + Props.getProgramName()));
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();

            /*URLConnection connection = url.openConnection();

            OutputStream outputStream = new FileOutputStream(new File(Props.getPathHome() + "/" + Props.getProgramName()));

            InputStream inputStream = connection.getInputStream();
            Files.copy()
            Files.copy(inputStream, new File(Props.getPathHome() + "/" + Props.getProgramName()).toPath(), new CopyOption());*/

            /*InputStream in = new BufferedInputStream(url.openStream());
            FileOutputStream out = new FileOutputStream(fileName);

            Props.getLogger().log(Level.INFO, "Начата загрузка файла обновления.");

            int size = in.available() / 1024;
            byte buffer[] = new byte[in.available()];
            while (in.read(buffer) != -1) {
                out.write(buffer);
            }
            Props.getLogger().log(Level.INFO, "Окончена загрузка файла обновления. Размер: " + size + " Kb.");

            in.close();
            out.close();*/

            return true;
        } catch (IOException e) {
            Props.getLogger().log(Level.WARNING, "Ошибка при загрузке файла обновления.", e);
            return false;
        }

    }


    public class JSONRegistration {
        public String Registration;
        public String LastVersion;

        public String getRegistration() {
            return Registration;
        }

        public String getLastVersion() {
            return LastVersion;
        }

        public void setRegistration(String Registration) {
            Registration = Registration;
        }

        public void setLastVersion(String lastVersion) {
            LastVersion = lastVersion;
        }
    }

}
