/*
 * Copyright 2024 piercemar.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.doclerk.cli;

import io.github.doclerk.core.exec.DoclerkExecutionPlan;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.doclerk.core.exception.DoclerkException;
import io.github.doclerk.core.model.DocPart;
import io.github.doclerk.core.model.DocRoot;
import io.github.doclerk.core.model.std.BlankDocRoot;
import io.github.doclerk.core.model.std.CompoundDocRoot;
import io.github.doclerk.core.model.std.CompoundDocRoot.DocRootMergePolicy;
import io.github.doclerk.core.module.EnvironmentModuleContext;
import io.github.doclerk.core.module.ModuleContextKeys;
import io.github.doclerk.core.module.DoclerkModule;
import io.github.doclerk.core.module.DoclerkModuleInfo;
import io.github.doclerk.core.module.HashMapModuleContext;
import io.github.doclerk.core.module.ModuleContext;
import io.github.doclerk.core.module.SystemModuleContext;
import java.io.Reader;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import io.github.doclerk.core.exec.DoclerkExecution;
import io.github.doclerk.core.exec.DoclerkModuleExecution;

@Command(name = "doclerk", mixinStandardHelpOptions = true, version = "0.1.0",
        description = "Generates documents based on the supplied template and project metadata")
public class Doclerk implements Callable<Integer> {

    private static final Logger logger = LogManager.getLogger(Doclerk.class);

    @Option(names = {"-w", "--work-dir"}, description = "work directory", defaultValue = ".")
    private Path workDir;

    @Option(names = {"-f", "--input-file"}, description = "input file", defaultValue = "doclerk.yaml")
    private Path inputFile;

    @Option(names = {"-X", "--no-exit"}, description = "prevents the JVM from exiting at the end")
    private static boolean noExit = false;

    @Option(names = {"-F", "--no-fail"}, description = "fails silently")
    private static boolean noFail = false;

    public Doclerk() {
        this(Paths.get("doclerk.yaml"));
    }

    public Doclerk(Path inputFile) {
        this(inputFile, Paths.get("."));
    }

    public Doclerk(Path inputFile, Path workDir) {
        this.inputFile = inputFile;
        this.workDir = workDir;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Doclerk()).execute(args);
        logger.info("noExit={}", noExit);
        logger.info("noFail={}", noFail);
        if (noExit) {
            if (exitCode != 0 && !noFail) {
                throw new DoclerkException("Exit code: " + exitCode);
            } else {
                logger.error("exitCode={}", exitCode);
            }
        } else {
            if (exitCode != 0 && noFail) {
                logger.warn("exitCode={}", exitCode);
            }
            System.exit(noFail ? 0 : exitCode);
        }
    }

    @Override
    public Integer call() throws Exception {
        Map<String, String> options = new HashMap<>(2, 1.0f);
        options.put(ModuleContextKeys.INPUT_FILE_URI, sanitizeUri(inputFile));
        logger.info("== Input file\t: {}", options.get(ModuleContextKeys.INPUT_FILE_URI));
        options.put(ModuleContextKeys.WORK_DIR_URI, sanitizeUri(workDir));
        logger.info("== Work directory\t: {}", options.get(ModuleContextKeys.WORK_DIR_URI));

        final Map<String, DoclerkModule> foundModules = DoclerkModule.findAll().stream()
                .collect(Collectors.toMap(
                        m -> m.moduleInfo().moduleName(),
                        m -> m
                ));
        logger.info("== Found modules\t:");
        foundModules.values().stream().map(DoclerkModule::moduleInfo).map(DoclerkModuleInfo::displayName).sorted().forEachOrdered(dn -> logger.info("==\t{}", dn));
        final ModuleContext env = new EnvironmentModuleContext();
        final ModuleContext sys = new SystemModuleContext();
        final ModuleContext opt = new HashMapModuleContext(options);

        DoclerkExecutionPlan dep;
        try (Reader reader = Files.newBufferedReader(inputFile)) {
            dep = new YamlExecutionPlan(reader);
        }
        int failCount = 0;
        for (DoclerkExecution exec : dep.getExecutions()) {
            try {
                DocRoot docRoot = new BlankDocRoot();
                for (DoclerkModuleExecution dme : exec.getModuleExecutions()) {
                    final String modName = dme.getModuleName();
                    final Map<String, String> modParams = dme.getParameters();
                    DoclerkModule mod = foundModules.get(modName);
                    if (mod != null) {
                        docRoot = new CompoundDocRoot(new DocRootMergePolicy(
                                (a, b) -> a + "," + b,
                                (a, b) -> a + "-" + b,
                                (a, b) -> {
                                    Set<DocPart> r = new LinkedHashSet<>(a);
                                    r.addAll(b);
                                    return r;
                                }),
                                docRoot,
                                mod.withContext(env).withContext(sys).withContext(opt).withContext(new HashMapModuleContext(modParams)).run(docRoot));
                    } else {
                        throw new DoclerkException("Module not found: " + modName);
                    }
                }
            } catch (Exception e) {
                failCount++;
            }
        }
        return failCount;
    }

    private String sanitizeUri(Path path) {
        return path.toUri().normalize().toString();
    }

}
