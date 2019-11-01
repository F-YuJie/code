package com.bw.fyj.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

public class UpLoadFile {

	public static String fileUp(MultipartFile file) throws Exception {
		// 处理图片文件名
		String originalFilename = file.getOriginalFilename();
		int lastIndexOf = originalFilename.lastIndexOf(".");
		String imgSuffix = originalFilename.substring(lastIndexOf);
		String uuid = UUID.randomUUID().toString();
		String imgName = uuid + imgSuffix;
		// 获取类路径
		File path = new File(ResourceUtils.getURL("classpath:").getPath());
		// 保存到数据库字段中的图片路径
		String paths = "static/fileUpload/" + imgName;
		// 服务器端保存图片的路径
		File upload = new File(path.getAbsolutePath(), "static/fileUpload/" + imgName);
		if (!upload.getParentFile().exists()) {
			upload.getParentFile().mkdirs();
		}
		file.transferTo(upload);
		return paths;
	}

}
