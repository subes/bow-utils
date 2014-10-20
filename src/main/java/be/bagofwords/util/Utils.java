package be.bagofwords.util;

import be.bagofwords.ui.UI;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.Map.Entry;

public class Utils {

    public static <T> ArrayList<T> list() {
        return new ArrayList<>();
    }

    public static int minFraq(int value, int divisor) {
        int result = value / divisor;
        if (value % divisor != 0)
            return result + 1;
        else
            return result;
    }

    public static int minLog(int val) {
        int exp = 0;
        int currVal = 1;
        while (currVal < val) {
            exp++;
            currVal *= 2;
        }
        return exp;
    }

    public static int readInt(byte[] data, int position, int numOfBytes) {
        int result = 0;
        for (int i = 0; i < numOfBytes; i++)
            if (i > 0)
                result = (result << 8) | ((int) data[position + i] & 0xFF);
            else
                result = (result << 8) | ((int) data[position + i]);
        return result;

    }

    public static void writeInt(byte[] data, int position, int value, int numOfBytes) {
        for (int i = numOfBytes - 1; i >= 0; i--) {
            data[position + i] = (byte) value;
            value = value >> 8;
        }
    }

    private static final int[] starts;

    static {
        starts = new int[5];
        for (int i = 1; i <= 4; i++)
            starts[i] = -(1 << (i * 8 - 1));
    }

    public static String getIP() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface iface : Collections.list(ifaces)) {
                if (!iface.isLoopback()) {
                    Enumeration<InetAddress> raddrs = iface.getInetAddresses();
                    for (InetAddress raddr : Collections.list(raddrs)) {
                        String ip = raddr.toString().replaceFirst("/", "");
                        if (ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))
                            return ip;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    public static String getGitVersion() {
        File file = new File(".git/logs/HEAD");
        if (file.exists()) {
            String lastLine = readLastLine(file);
            String[] parts = lastLine.split(" ");
            return parts[1];
        } else {
            throw new RuntimeException("Could not find .git directory!");
        }
    }

    private static String readLastLine(File file) {
        String lastLine = null;
        try {
            BufferedReader rdr = new BufferedReader(new FileReader(file));
            String line;
            while ((line = rdr.readLine()) != null) {
                lastLine = line;
            }
            return lastLine;
        } catch (IOException exp) {
            throw new RuntimeException(exp);
        }
    }

    public static <T extends Comparable, S extends Object> ArrayList<Pair<T, S>> reverseList(HashMap<S, T> map) {
        ArrayList<Pair<T, S>> result = new ArrayList<>();
        for (Entry<S, T> entry : map.entrySet())
            result.add(new Pair<>(entry.getValue(), entry.getKey()));
        return result;
    }

    public static <S extends Comparable, T extends Object> ArrayList<Pair<S, T>> list(HashMap<S, T> map) {
        ArrayList<Pair<S, T>> result = new ArrayList<>();
        for (Entry<S, T> entry : map.entrySet())
            result.add(new Pair<>(entry.getKey(), entry.getValue()));
        return result;
    }

    public static <T extends Object> ArrayList<T> list(List<T> origList, T obj) {
        ArrayList<T> result = new ArrayList<>(origList);
        result.add(obj);
        return result;
    }

    public static <T extends Object> ArrayList<T> list(T... objs) {
        ArrayList<T> result = new ArrayList<>();
        Collections.addAll(result, objs);
        return result;
    }

    public static <S extends Object, T extends Object> Pair<S, T> pair(S obj1, T obj2) {
        return new Pair<>(obj1, obj2);
    }

    public static <T extends Object, S extends Object> HashMap<T, S> map() {
        return new HashMap<>();
    }

    public static <T extends Object, S extends Object> HashMap<T, S> map(ArrayList<Pair<T, S>> values) {
        HashMap<T, S> res = map();
        for (Pair<T, S> val : values)
            res.put(val.getFirst(), val.getSecond());
        return res;
    }

    public static String getStackTrace(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.getBuffer().toString();
    }

    public static void threadSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().notify();
            //we don't like interrupted exceptions. Who came up with this idea?
        }
    }

    public static <T extends Object> List<T> filter(List<T> list, Filter<T> filter) {
        List<T> result = new ArrayList<>();
        for (T obj : list) {
            if (filter.accept(obj)) {
                result.add(obj);
            }
        }
        return result;
    }

    public static void addLibraryPath(String pathToAdd) {
        try {
            final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);

            //get array of paths
            final String[] paths = (String[]) usrPathsField.get(null);

            //check if the path to add is already present
            for (String path : paths) {
                if (path.equals(pathToAdd)) {
                    return;
                }
            }

            //add the new path
            final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
            newPaths[newPaths.length - 1] = pathToAdd;
            usrPathsField.set(null, newPaths);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
