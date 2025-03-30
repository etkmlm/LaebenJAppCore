package com.laeben.core.entity;

import com.laeben.core.LaebenApp;
import com.laeben.core.util.StrUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Advanced OS-based IO ecosystem.
 **/
public class Path{
    private java.nio.file.Path root;
    private Boolean isDirLocal;

    private Path(java.nio.file.Path root){
        this.root = root;
    }

    /**
     * Begin path from the system path.
     * @param r system path
     * @return new path
     **/
    public static Path begin(java.nio.file.Path r){
        return new Path(r);
    }

    /**
     * Set this path as a directory or not by the given value. <i>(forever)</i>
     **/
    public Path forceSetDir(boolean isDirectory){
        isDirLocal = isDirectory;
        return this;
    }

    /**
     * @return absolute path
     **/
    @Override
    public String toString(){
        return root.toString();
    }

    /**
     * @return absolute path with escape characters
     **/
    public String escapeString(){
        return root.toString().replace(" ", "\\ ");
    }

    /**
     * @return absolute path with quotes
     **/
    public String quoteString(){
        return "\"" + root.toString() + "\"";
    }

    /**
     * Concatenate with one or more paths.
     * @return new path
     **/
    public Path to(String... keys){
        try{
            return new Path(Paths.get(root.toString(), keys));
        }
        catch (NullPointerException e){
            return null;
        }
    }

    /**
     * Concatenate with one path.
     * @return new path
     **/
    public Path to(String key){
        return new Path(root.resolve(key).normalize());
    }

    /**
     * @return parent path
     **/
    public Path parent(){
        return new Path(root.getParent());
    }

    /**
     * Convert to system file.
     * @return system file
     **/
    public File toFile(){
        return root.toFile();
    }

    /**
     * Move path.
     * @param newPath path of the new file or directory
     **/
    public void move(Path newPath){
        copy(newPath);

        delete();

        root = newPath.toFile().toPath();
    }

    /**
     * @return is path existing
     **/
    public boolean exists(){
        return toFile().exists();
    }

    /**
     * Creates parent directories of the path if not exist.
     * @return the path
     **/
    public Path prepare(){
        File file = toFile();
        if (isDirectory()){
            file.mkdirs();
            file.mkdir();
        }
        else
            new File(file.getParent()).mkdirs();

        return this;
    }

    /**
     * Get all files in the directory.
     * @return files as path
     **/
    public List<Path> getFiles(){
        File file = toFile();

        if (!isDirectory())
            return List.of();

        File[] files = file.listFiles();

        if (files == null)
            return List.of();

        return Arrays.stream(files).map(x -> new Path(x.toPath())).collect(Collectors.toList());
    }

    /**
     * Read all bytes from input stream.
     * @return bytes
     **/
    public byte[] readAllBytes(InputStream str) throws IOException {
        List<Integer> bytes = new ArrayList<>();

        int read;
        while ((read = str.read()) != -1){
            bytes.add(read);
        }

        int size = bytes.size();
        byte[] all = new byte[size];
        for (int i = 0; i < size; i++)
            all[i] = bytes.get(i).byteValue();

        return all;
    }

    /**
     * Open path as gzip file.
     * @return content as bytes
     **/
    public byte[] openAsGzip() throws IOException {
        try(InputStream file = Files.newInputStream(root.toFile().toPath());
            GZIPInputStream gzip = new GZIPInputStream(file)){

            return gzip.readAllBytes();
        }
    }

    /**
     * Get size of the file.
     * @return size as long
     **/
    public long getSize(){
        try{
            return Files.size(root);
        }
        catch (IOException e){
            return 0;
        }
    }

    /**
     * Write content to path.
     * <br/>
     * Writes content to the file from the beginning.
     **/
    public void write(String content){
        try{
            prepare();
            Files.write(root, content.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e){
            LaebenApp.handleException(e);
        }
    }


    /**
     * Append content to path.
     * <br/>
     * Writes content to the file from the last byte.
     **/
    public void append(String content){
        prepare();
        try (FileWriter writer = new FileWriter(root.toFile(), true)) {
            writer.write(content);
        }
        catch (IOException e){
            LaebenApp.handleException(e);
        }
    }
    /**
     * Read file as string.
     * @return content
     **/
    public String read(){
        try{
            prepare();
            return Files.readString(root);
        }
        catch (IOException e){
            return "";
        }
    }

    /**
     * Delete the file or directory.
     * @return is successful
     **/
    public boolean delete(){
        if (root.toFile().delete() || !isDirectory())
            return false;

        getFiles().forEach(Path::delete);

        return root.toFile().delete();
    }

    /**
     * Get name of the path.
     * <br/>
     * <i>Example: </i> myfile.txt for file
     *                  mydir for directory
     * @return name
     **/
    public String getName(){
        return root.toFile().getName();
    }

    /**
     * Get name of the path without extension.
     * <br/>
     * <i>Example: </i> myfile for file
     *                  mydir for directory
     * @return name
     **/
    public String getNameWithoutExtension(){
        if (isDirectory())
            return getName();

        String[] spl = getName().split("\\.");
        return getName().substring(0, getName().length() - spl[spl.length - 1].length() - 1);
    }

    /**
     * Get extension of the path.
     * <b>Example: </b> txt for file (without dot)
     *                  null for directory
     * @return extension
     **/
    public String getExtension(){
        if (!getName().contains("."))
            return null;
        String[] all = getName().split("\\.");
        return all[all.length - 1];
    }

    private ZipArchiveEntry getEntry(Path root, Path p){
        String rootPath = root.toString();
        String newPath = p.toString().substring(rootPath.length()).replace('\\', '/');
        if (newPath.startsWith("/"))
            newPath = root.getName() + newPath;
        return new ZipArchiveEntry(p.toFile(), newPath.isEmpty() ? p.getName() : newPath);
    }

    private void zipEntry(ZipArchiveOutputStream stream, Path root, Path p){
        try {
            stream.putArchiveEntry(getEntry(root, p));
        } catch (IOException ignored) {

        }
        if (p.isDirectory()){
            p.getFiles().forEach(x -> zipEntry(stream, root, x));
        }
        else{
            try(FileInputStream str = new FileInputStream(p.toFile())) {
                stream.write(str.readAllBytes());
            } catch (IOException ignored) {}
        }

    }

    /**
     * Compress path as zip.
     * @param fileName file name of the zip
     **/
    public void zip(Path fileName){
        try(ZipArchiveOutputStream stream = new ZipArchiveOutputStream(fileName.toFile())){
            zipEntry(stream, this, this);
            stream.closeArchiveEntry();
        }
        catch (IOException e){
            LaebenApp.handleException(e);
        }
    }

    /**
     * Get the first entry from the zip file.
     * @return name of the entry
     **/
    public String getFirstZipEntry(){
        try(ZipArchiveInputStream stream = new ZipArchiveInputStream(Files.newInputStream(toFile().toPath()))){
            return stream.getNextEntry().getName();
        }
        catch (IOException e){
            LaebenApp.handleException(e);
            return null;
        }
    }

    /**
     * Get the main folder of the zip file.
     * @return name of the folder
     **/
    public String getZipMainFolder(){
        String entry = getFirstZipEntry();
        if (entry == null)
            return null;

        return entry.contains("/") ? entry.split("/")[0] : null;
    }

    public static class PossibilityResult<T>{
        private final int order;
        private final T value;

        public PossibilityResult(int order, T value){
            this.order = order;
            this.value = value;
        }

        public int getOrder(){
            return order;
        }

        public T getValue(){
            return value;
        }
    }

    /**
     * Get the specified entry from the zip file.
     * @param paths possible relative paths of the entry (foo/bar.json, foo/foo, etc.)
     * @return entry byte stream
     **/
    public PossibilityResult<ByteArrayOutputStream> tryGetZipEntry(String... paths){
        try(ZipArchiveInputStream stream = new ZipArchiveInputStream(Files.newInputStream(toFile().toPath()));
            ByteArrayOutputStream bytes = new ByteArrayOutputStream()){
            ZipArchiveEntry e;
            while ((e = stream.getNextEntry()) != null) {
                int px = -1;

                for (int i = 0; i < paths.length; i++){
                    if (paths[i].equals(e.getName())){
                        px = i;
                        break;
                    }
                }

                if (px == -1)
                    continue;


                if (!stream.canReadEntryData(e))
                    return null;
                int read;
                byte[] buff = new byte[4096];
                while ((read = stream.read(buff)) != -1)
                    bytes.write(buff, 0, read);

                return new PossibilityResult<>(px, bytes);
            }
            return null;
        }
        catch (IOException e){
            LaebenApp.handleException(e);
            return null;
        }
    }

    /**
     * Get the specified entry as string from the zip file.
     * @param paths possible relative paths of the entry (foo/bar.json, foo/foo, etc.)
     * @return read string
     **/
    public PossibilityResult<String> tryReadZipEntry(String... paths){
        var n = tryGetZipEntry(paths);
        try(var str = n.getValue()){
            return new PossibilityResult<>(n.getOrder(), str.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private void extract(Path destination, ArchiveInputStream stream, List<String> exclude) throws IOException {
        ArchiveEntry entry;

        while ((entry = stream.getNextEntry()) != null){
            String name = StrUtil.pure(entry.getName(), new char[]{'/'});
            if (exclude.stream().anyMatch(a -> StrUtil.pure(a).equals(name)))
                continue;

            Path pp = destination.to(name);
            File ff = pp.toFile();
            if (entry.isDirectory())
                ff.mkdirs();
            else {
                new File(ff.getParent()).mkdirs();
                try(FileOutputStream f = new FileOutputStream(ff)) {
                    byte[] buffer = new byte[16384];
                    int read;
                    while ((read = stream.read(buffer)) != -1)
                        f.write(buffer, 0, read);
                }
                pp.execPosix();

                new File(ff.getPath()).setLastModified(entry.getLastModifiedDate().getTime());
            }
        }
    }

    private void extractTar(Path destination, List<String> exclude){
        try(GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(toFile().toPath()));
            TarArchiveInputStream tar = new TarArchiveInputStream(gzip)){
            extract(destination, tar, exclude);
        }
        catch (IOException e){
            LaebenApp.handleException(e);
        }
    }

    private void extractZip(Path destination, List<String> exclude){
        try(FileInputStream file = new FileInputStream(root.toFile());
            ZipArchiveInputStream zip = new ZipArchiveInputStream(file)){
            extract(destination, zip, exclude);
        }
        catch (IOException e){
            LaebenApp.handleException(e);
        }
    }

    /**
     * Mark file with full access for Unix systems.
     **/
    public void execPosix(){
        try{
            HashSet<PosixFilePermission> set = new HashSet<>();
            set.add(PosixFilePermission.OWNER_WRITE);
            set.add(PosixFilePermission.GROUP_WRITE);
            set.add(PosixFilePermission.OTHERS_WRITE);
            set.add(PosixFilePermission.OWNER_READ);
            set.add(PosixFilePermission.GROUP_READ);
            set.add(PosixFilePermission.OTHERS_READ);
            set.add(PosixFilePermission.OWNER_EXECUTE);
            set.add(PosixFilePermission.GROUP_EXECUTE);
            set.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(root, set);
        }
        catch (IOException e){
            LaebenApp.handleException(e);
        }
        catch (UnsupportedOperationException ignored){

        }
    }

    /**
     * Extract the tar.gz, zip, or jar files.
     * @param destination destination directory, not file
     * @param exclude excluded file names
     **/
    public void extract(Path destination, List<String> exclude){
        if (destination == null)
            destination = new Path(root.getParent());

        if (exclude == null)
            exclude = List.of();

        if (getExtension().equals("gz")){
            extractTar(destination, exclude);
        }
        else if (getExtension().equals("zip") || getExtension().equals("jar"))
            extractZip(destination, exclude);
    }

    /**
     * @return is this path is directory or not
     **/
    public boolean isDirectory(){
        return isDirLocal != null ? isDirLocal : (exists() ? root.toFile().isDirectory() : getExtension() == null);
    }

    /**
     * Copy the path to the destination. File will be overwritten default.
     * @param destination destination file or directory
     */
    public void copy(Path destination){
        copy(destination, true);
    }

    /**
     * Copy the path to the destination.
     * @param destination destination file or directory
     * @param overwrite should be overwritten if exists
     **/
    public void copy(Path destination, boolean overwrite){
        try{
            //destination.prepare();
            if (destination.exists() && !isDirectory()){
                if (!overwrite)
                    return;
                destination.delete();
            }
            if (isDirectory())
                getFiles().forEach(x -> x.copy(destination.to(x.getName())));
            else{
                destination.forceSetDir(false).prepare();
                Files.copy(root, destination.root);
            }
        }catch (IOException e){
            LaebenApp.handleException(e);
        }
    }

    /**
     * Read all bytes of the file.
     * @return bytes
     **/
    public byte[] readBytes(){
        try {
            return Files.readAllBytes(root);
        } catch (Exception e) {
            LaebenApp.handleException(e);
            return new byte[0];
        }
    }
    @Override
    public boolean equals(Object obj){
        return obj instanceof Path && obj.toString().equals(toString());
    }
}
