package com.origin.framework.file.util;

import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.file.domain.request.BasicFileRequest;
import com.origin.framework.file.domain.request.CombineRequest;
import com.origin.framework.file.domain.response.CombineResponse;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CombineFileUtil {
    /**
     * Combines two files based on a specified request.
     *
     * @param originVertxContext The OriginWebVertx context.
     * @param request            The CombineRequest containing details of the left and right files to join, and key indices.
     * @return A Future object that will contain a list of combined responses when completed.
     */
    public static Future<List<CombineResponse>> combineFile(OriginWebVertxContext originVertxContext, CombineRequest request) {

        return Future.future(promise -> {
            if (request == null || request.getLeftFile().isEmpty() || request.getRightFile().isEmpty()) {
                promise.fail("Unknown Combine Request.");
                return;
            }
            Map<String, CombineResponse> map = new HashMap<>();

            ReadFileUtil.readFile(originVertxContext, new BasicFileRequest(request.getLeftFile()), (row, resultReport) -> {
                if (!row.isEmpty()) {
                    CombineResponse cr = new CombineResponse();
                    String[] args = row.split(",");
                    try {
                        String key = args[request.getLeftJoinKeyIndex()];
                        cr.withKey(key);
                        Arrays.stream(request.getLeftJoinFields()).forEach(i -> cr.addColumn(args[i].replaceAll("\r", "")));

                        map.put(key, cr);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        promise.fail(e);
                    }

                } else {
                    log.warn("row at [{}] is empty.", resultReport.getTotalSize().get() - 1);
                }


            }, () -> {
                ReadFileUtil.readFile(originVertxContext, new BasicFileRequest(request.getRightFile()), (row, resultReport) -> {
                    if (!row.isEmpty()) {
                        String[] args = row.split(",");
                        try {
                            String key = args[request.getRightJoinKeyIndex()];
                            if (map.containsKey(key)) {
                                CombineResponse cr = map.get(key);
                                Arrays.stream(request.getRightJoinFields()).forEach(i -> cr.addColumn(args[i].replaceAll("\r", "")));
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            promise.fail(e);
                        }

                    } else {
                        log.warn("row at [{}] is empty.", resultReport.getTotalSize().get() - 1);
                    }
                }, () -> {
                    promise.complete(map.values().stream().toList());
                });

            });

        });
    }
}
