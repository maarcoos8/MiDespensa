package com.example.codepractica.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageHelper {
    
    private Activity activity;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Uri currentPhotoUri;
    private String currentPhotoPath;
    private ImageSelectionCallback callback;
    
    public interface ImageSelectionCallback {
        void onImageSelected(String imagePath);
        void onImageDeleted();
    }
    
    public ImageHelper(Activity activity, 
                      ActivityResultLauncher<Intent> cameraLauncher,
                      ActivityResultLauncher<Intent> galleryLauncher,
                      ActivityResultLauncher<String> permissionLauncher,
                      ImageSelectionCallback callback) {
        this.activity = activity;
        this.cameraLauncher = cameraLauncher;
        this.galleryLauncher = galleryLauncher;
        this.permissionLauncher = permissionLauncher;
        this.callback = callback;
    }
    
    public void showImageSourceDialog(boolean hasCurrentImage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Seleccionar imagen");
        
        String[] options;
        if (hasCurrentImage) {
            options = new String[]{"Tomar foto", "Elegir de galería", "Eliminar foto"};
        } else {
            options = new String[]{"Tomar foto", "Elegir de galería"};
        }
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    openCamera();
                    break;
                case 1:
                    openGallery();
                    break;
                case 2:
                    if (hasCurrentImage) {
                        deleteImage();
                    }
                    break;
            }
        });
        
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
    
    private void openCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(activity,
                        activity.getPackageName() + ".fileprovider",
                        photoFile);
                
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        } catch (IOException e) {
            Toast.makeText(activity, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhotoIntent);
    }
    
    private void deleteImage() {
        new AlertDialog.Builder(activity)
                .setTitle("Eliminar imagen")
                .setMessage("¿Estás seguro de que quieres eliminar la imagen?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (callback != null) {
                        callback.onImageDeleted();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(activity.getFilesDir(), "images");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    public void handleCameraResult() {
        if (currentPhotoPath != null && callback != null) {
            callback.onImageSelected(currentPhotoPath);
        }
    }
    
    public void handleGalleryResult(Intent data) {
        if (data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            String imagePath = copyUriToInternalStorage(selectedImageUri);
            if (imagePath != null && callback != null) {
                callback.onImageSelected(imagePath);
            }
        }
    }
    
    private String copyUriToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(activity, "No se pudo abrir la imagen", Toast.LENGTH_SHORT).show();
                return null;
            }

            File file = createImageFile(); // Reutilizamos el método para crear un archivo destino

            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            }
            inputStream.close();
            
            return file.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, "Error al copiar la imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    
    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }
}
