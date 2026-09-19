package com.laeben.core.entity;

import com.laeben.core.entity.exception.StopException;
import com.laeben.core.event.context.EventContext;
import com.laeben.core.event.function.ProgressFunction;
import com.laeben.core.event.payload.ProgressPayload;
import com.laeben.core.util.StrUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.*;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Advanced OS-based IO ecosystem.
 **/
public class Path{
    public static class TransferContext extends EventContext{
        private final Path source;
        private final Path destination;

        /**
         * @param source source file
         * @param destination destination file
         **/
        public TransferContext(Path source, Path destination) {
            super(source.getName());

            this.source = source;
            this.destination = destination;
        }

        /**
         * @return source file
         */
        public Path getSourceFile() {
            return source;
        }

        /**
         * @return destination file, null if the destination is a zip entry
         */
        public Path getDestinationFile() {
            return destination;
        }
    }

    public static class ExtractionItemContext extends EventContext{
        /**
         * @param filename file name
         **/
        public ExtractionItemContext(String filename) {
            super(filename);
        }

        /**
         * @return file name
         */
        public String getFilename() {
            return getLabel();
        }
    }

    public static class ParentItemContext extends EventContext{
        private final Path parent, file;

        public ParentItemContext(Path parent, Path file) {
            super(file.getName());

            this.parent = parent;
            this.file = file;
        }

        /**
         * @return destination parent
         **/
        public Path getParent() {
            return parent;
        }

        /**
         * @return destination file
         **/
        public Path getFile() {
            return file;
        }
    }

    private static final int BUFFER_SIZE = 16384;

    private final java.nio.file.Path root;
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
    public void move(Path newPath) throws IOException, StopException {
        move(newPath, null);
    }

    /**
     * Move path.
     * @param newPath path of the new file or directory
     * @param onProgress progress function
     **/
    public void move(Path newPath, ProgressFunction onProgress) throws IOException, StopException {
        copy(newPath);

        delete();

        //root = newPath.toFile().toPath();
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
     * Open path as gzip file.
     * @return content as bytes
     **/
    public byte[] openAsGzip() throws IOException, StopException {
        try(InputStream file = Files.newInputStream(root.toFile().toPath());
            GZIPInputStream gzip = new GZIPInputStream(file)){

            return gzip.readAllBytes();
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }

    /**
     * Get size of the file.
     * @return size as long
     **/
    public long getSize() throws StopException, IOException {
        try{
            return Files.size(root);
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }

    /**
     * Write content to path.
     * <br/>
     * Writes content to the file from the beginning.
     **/
    public void write(String content) throws StopException, IOException {
        try{
            prepare();
            Files.write(root, content.getBytes(StandardCharsets.UTF_8));
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }


    /**
     * Append content to path.
     * <br/>
     * Writes content to the file from the last byte.
     **/
    public void append(String content) throws IOException, StopException {
        prepare();
        try (FileWriter writer = new FileWriter(root.toFile(), true)) {
            writer.write(content);
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }
    /**
     * Read file as string.
     * @return content
     **/
    public String read() throws IOException, StopException {
        try{
            prepare();
            return Files.readString(root);
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
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

    private void zipEntry(ZipArchiveOutputStream stream, Path root, Path p, ProgressFunction onProgress) throws StopException, IOException {
        stream.putArchiveEntry(getEntry(root, p));
        if (p.isDirectory()){
            for (var x : p.getFiles()){
                zipEntry(stream, root, x, onProgress);
            }
        }
        else{
            try(FileInputStream str = new FileInputStream(p.toFile())) {
                transferStreams(new byte[BUFFER_SIZE], str, stream, ProgressPayload.create(onProgress, new TransferContext(p, null), p.getSize()));
            }
        }

    }

    /**
     * Compress path as zip.
     * @param fileName file name of the zip
     **/
    public void zip(Path fileName) throws IOException, StopException {
        zip(fileName, null);
    }

    /**
     * Compress path as zip.
     * @param fileName file name of the zip
     * @param onProgress progress function
     **/
    public void zip(Path fileName, ProgressFunction onProgress) throws IOException, StopException {
        try(ZipArchiveOutputStream stream = new ZipArchiveOutputStream(fileName.toFile())){
            zipEntry(stream, this, this, onProgress);
            stream.closeArchiveEntry();
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }

    /**
     * Get the first entry from the zip file.
     * @return name of the entry
     **/
    public String getFirstZipEntry() throws IOException, StopException {
        try(ZipArchiveInputStream stream = new ZipArchiveInputStream(Files.newInputStream(toFile().toPath()))){
            return stream.getNextEntry().getName();
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }

    /**
     * Get the main folder of the zip file.
     * @return name of the folder
     **/
    public String getZipMainFolder() throws IOException, StopException {
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
    public PossibilityResult<ByteArrayOutputStream> tryGetZipEntry(String... paths) throws IOException, StopException {
        return tryGetZipEntry(null, paths);
    }

    /**
     * Get the specified entry from the zip file.
     * @param paths possible relative paths of the entry (foo/bar.json, foo/foo, etc.)
     * @param onProgress progress function
     * @return entry byte stream
     **/
    public PossibilityResult<ByteArrayOutputStream> tryGetZipEntry(ProgressFunction onProgress, String... paths) throws IOException, StopException {
        try(ZipArchiveInputStream stream = new ZipArchiveInputStream(Files.newInputStream(toFile().toPath()));
            ByteArrayOutputStream bytes = new ByteArrayOutputStream()){
            ZipArchiveEntry e;

            byte[] buff = new byte[BUFFER_SIZE];

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

                transferStreams(buff, stream, bytes, ProgressPayload.create(onProgress, new ExtractionItemContext(e.getName()), e.getSize()));

                return new PossibilityResult<>(px, bytes);
            }
            return null;
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }

    /**
     * Get the specified entry as string from the zip file.
     * @param paths possible relative paths of the entry (foo/bar.json, foo/foo, etc.)
     * @return read string
     **/
    public PossibilityResult<String> tryReadZipEntry(String... paths) throws IOException, StopException {
        var n = tryGetZipEntry(paths);
        try(ByteArrayOutputStream str = n.getValue()){
            return new PossibilityResult<>(n.getOrder(), str.toString());
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
        catch (Exception e) {
            return null;
        }
    }



    private void extract(Path destination, ArchiveInputStream<?> stream, List<String> exclude, ProgressFunction onProgress) throws IOException, StopException {
        ArchiveEntry entry;

        long totalSize = this.getSize();

        byte[] buffer = new byte[BUFFER_SIZE];

        while ((entry = stream.getNextEntry()) != null){
            if (Thread.currentThread().isInterrupted())
                throw new StopException();

            String name = StrUtil.pure(entry.getName(), new char[]{'/'});
            if (exclude.stream().anyMatch(a -> StrUtil.pure(a).equals(name)))
                continue;

            Path pp = destination.to(name);

            if (onProgress != null) onProgress.onProgress(stream.getBytesRead(), totalSize, new ParentItemContext(destination, pp));

            File ff = pp.toFile();
            if (entry.isDirectory())
                ff.mkdirs();
            else {
                new File(ff.getParent()).mkdirs();
                try(FileOutputStream f = new FileOutputStream(ff)) {
                    transferStreams(buffer, stream, f, ProgressPayload.create(onProgress, new ExtractionItemContext(entry.getName()), entry.getSize()));
                }
                pp.execPosix();

                new File(ff.getPath()).setLastModified(entry.getLastModifiedDate().getTime());
            }
        }
    }

    private void extractTar(Path destination, List<String> exclude, ProgressFunction onProgress) throws StopException, IOException {
        try(GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(toFile().toPath()));
            TarArchiveInputStream tar = new TarArchiveInputStream(gzip)){
            extract(destination, tar, exclude, onProgress);
        }
    }

    private void extractZip(Path destination, List<String> exclude, ProgressFunction onProgress) throws StopException, IOException {
        try(FileInputStream file = new FileInputStream(root.toFile());
            ZipArchiveInputStream zip = new ZipArchiveInputStream(file)){
            extract(destination, zip, exclude, onProgress);
        }
    }

    /**
     * Mark file with full access for Unix systems.
     **/
    public void execPosix() throws IOException {
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
        catch (UnsupportedOperationException ignored){

        }
    }

    /**
     * Extract the tar.gz, zip, or jar files.
     * @param destination destination directory, not file
     * @param exclude excluded file names
     **/
    public void extract(Path destination, List<String> exclude) throws StopException, IOException {
        extract(destination, exclude, null);
    }

    /**
     * Extract the tar.gz, zip, or jar files.
     * @param destination destination directory, not file
     * @param exclude excluded file names
     * @param onProgress progress function
     **/
    public void extract(Path destination, List<String> exclude, ProgressFunction onProgress) throws StopException, IOException {
        if (destination == null)
            destination = new Path(root.getParent());

        if (exclude == null)
            exclude = List.of();

        try{
            if (getExtension().equals("gz")){
                extractTar(destination, exclude, onProgress);
            }
            else if (getExtension().equals("zip") || getExtension().equals("jar"))
                extractZip(destination, exclude, onProgress);
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
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
     **/
    public void copy(Path destination) throws IOException, StopException {
        copy(destination, true);
    }

    private static void transferStreams(byte[] buffer, InputStream in, OutputStream out, ProgressPayload progress) throws IOException, StopException {
        assert progress != null;

        int read;
        while ((read = in.read(buffer)) > 0){
            if (Thread.currentThread().isInterrupted()) throw new StopException();
            out.write(buffer, 0, read);
            progress.onProgress(read);
        }
    }

    /**
     * Copy the path to the destination.
     * @param destination destination file or directory
     * @param overwrite should be overwritten if exists
     **/
    public void copy(Path destination, boolean overwrite) throws StopException, IOException {
        copy(destination, overwrite, null);
    }

    /**
     * Copy the path to the destination.
     * @param destination destination file or directory
     * @param overwrite should be overwritten if exists
     * @param onProgress progress function
     **/
    public void copy(Path destination, boolean overwrite, ProgressFunction onProgress) throws StopException, IOException {
        try{
            //destination.prepare();
            if (destination.exists() && !isDirectory()){
                if (!overwrite)
                    return;
                destination.delete();
            }
            if (isDirectory()){
                final var files = getFiles();
                final int size = files.size();
                for (int i = 0; i < size; i++) {
                    final var x = files.get(i);
                    final var dest = destination.to(x.getName());
                    if (onProgress != null) onProgress.onProgress(i + 1, size, new ParentItemContext(this, dest));
                    x.copy(dest, true, onProgress);
                }
            }
            else{
                destination.forceSetDir(false).prepare();
                try(final var inFile = new FileInputStream(toFile());
                    final var outFile = new FileOutputStream(destination.toFile())){
                    transferStreams(new byte[BUFFER_SIZE], inFile, outFile, ProgressPayload.create(onProgress, new TransferContext(this, destination), getSize()));
                }
            }
        }
        catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }

    /**
     * Read all bytes of the file.
     * @return bytes
     **/
    public byte[] readBytes() throws StopException, IOException {
        try {
            return Files.readAllBytes(root);
        } catch (InterruptedIOException | ClosedByInterruptException ignored){
            Thread.currentThread().interrupt();
            throw new StopException();
        }
    }
    @Override
    public boolean equals(Object obj){
        return obj instanceof Path && obj.toString().equals(toString());
    }
}
