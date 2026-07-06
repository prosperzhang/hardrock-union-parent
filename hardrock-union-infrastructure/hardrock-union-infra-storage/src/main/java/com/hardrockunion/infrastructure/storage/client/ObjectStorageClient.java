package com.hardrockunion.infrastructure.storage.client;

import com.hardrockunion.infrastructure.storage.model.StoragePutObjectRequest;
import com.hardrockunion.infrastructure.storage.model.StoragePutObjectResult;

public interface ObjectStorageClient {

    StoragePutObjectResult putObject(StoragePutObjectRequest request);
}
