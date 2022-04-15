package fr.becpg.artworks.compare;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.ImageComparisonUtil;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;

import fr.becpg.artworks.helper.ContentHelper;

@Service
public class ImageFileComparePlugin implements CompareDocumentPlugin {

	private static final String IMAGE_IDENTIFIER = "image";
	
	private static final String RESULT_FILE_NAME = "result";
	
	private static final String PNG_EXTENSION = ".png";

	@Autowired
	private ContentService contentService;

	@Autowired
	private MimetypeService mimetypeService;
	
	@Autowired
	private ContentHelper contentHelper;

	@Override
	public boolean accepts(NodeRef nodeRef) {
		
		ContentReader reader = this.contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

		String mimeType = mimetypeService.guessMimetype(null, reader);

		return mimeType.contains(IMAGE_IDENTIFIER);
		
	}

	@Override
	public File compare(NodeRef node1, NodeRef node2) throws IOException {
		
		File file1 = contentHelper.createContentFile(node1);
		File file2 = contentHelper.createContentFile(node2);
		
		BufferedImage image1 = ImageComparisonUtil.readImageFromResources(file1.getAbsolutePath());
        BufferedImage image2 = ImageComparisonUtil.readImageFromResources(file2.getAbsolutePath());
        
        File resultFile = TempFileProvider.createTempFile(RESULT_FILE_NAME, PNG_EXTENSION);

        ImageComparison imageComparison = new ImageComparison(image1, image2);
        
        ImageComparisonResult imageResult = imageComparison.compareImages();
        
        ImageComparisonUtil.saveImage(resultFile, imageResult.getResult());

        contentHelper.deleteFile(file1);
        contentHelper.deleteFile(file2);

		return resultFile;

	}
	
}
