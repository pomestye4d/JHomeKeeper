/*
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.vga.hk.core.api.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.exception.ExceptionUtils;

import java.io.BufferedReader;
import java.io.File;

public class ProcessManager {
    private final String[] command;
    private final String searchPattern;
    private final File workingDirectory;
    private final Logger logger;
    private final String procId;
    private Process proc;

    public ProcessManager(String procId, String[] command, String searchPattern, File workingDirectory) {
        this.command = command;
        this.searchPattern = searchPattern;
        this.workingDirectory = workingDirectory;
        this.procId = procId;
        logger = LoggerFactory.getLogger(getClass());
    }

    public void startProcess(){
        ExceptionUtils.wrapException(() ->{
            if(isWindows()){
                return;
            }
            var pid = getPid();
            if(pid != null){
                logger.info("process %s is running, pid = %s".formatted(procId, pid));
                for(int n=0; n < 10; n++){
                    if(pid != null) {
                        logger.info("stopping process");
                        executeCommandAndWait(new String[]{"kill", pid}, workingDirectory);
                        Thread.sleep(1000L);
                        pid = getPid();
                    }
                }
                logger.info("process %s is stopped".formatted(procId));
            }
            ProcessBuilder builder = new ProcessBuilder(command).directory(workingDirectory);
            proc = builder.start();
            pid = getPid();
            for(int n=0; n < 10; n++){
                if(pid != null) {
                    break;
                }
                Thread.sleep(1000L);
                pid = getPid();
            }
            logger.info("process %s is started, pid = %s".formatted(procId, pid));
        });
    }

    public void stopProcess(){
        if(isWindows()){
            return;
        }
        if(proc != null && proc.isAlive()){
            proc.destroy();
            logger.info("process %s is destroyed".formatted(procId));
        }
    }

    private boolean isWindows(){
        return System.getProperty("os.name").contains("Windows");
    }

    private String getPid(){
        var output = executeCommandAndWait(new String[]{"ps","aux"}, workingDirectory);
        var line = output.lines().filter(it -> it != null && it.contains(searchPattern)).findFirst().orElse(null);
        if(line == null){
            return null;
        }
        var idx1 = line.indexOf(" ");
        line = line.substring(idx1+1);
        return line.substring(0, line.indexOf(" "));
    }

    private String executeCommandAndWait(String[] command, File workingDirectory){
        return ExceptionUtils.wrapException(() ->{
            ProcessBuilder builder = new ProcessBuilder(command).directory(workingDirectory);
            Process  process = builder.start();
            process.waitFor();
            var sb = new StringBuilder();
            try(var ir = new BufferedReader(process.inputReader())){
                var line = ir.readLine();
                while (line != null){
                    if(!sb.isEmpty()){
                        sb.append("\n");
                    }
                    sb.append(line);
                    line = ir.readLine();
                }
            }
            return sb.toString();
        });
    }
}
