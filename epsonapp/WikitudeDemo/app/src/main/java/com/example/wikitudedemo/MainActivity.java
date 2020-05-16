package com.example.wikitudedemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.WearableArchitectView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private WearableArchitectView architectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        architectView = new WearableArchitectView(this);

        ArchitectStartupConfiguration configuration = new ArchitectStartupConfiguration();
        configuration.setLicenseKey("z7tHao+2imWDz0SiiLi+vEQQQGQumzStTe6VeEvxiigoZivp5Wjawb9QwbGGyJFWZTdvrinZ1/+HlHq2N7yaieDRo98D1Ks1sLXlT1gw7hnvyx0d0MrPUXqmP9DtZE+KzsKIUjTwc4Ac+DmK3qEYrHRJyAg6CerMnQRGAExsnhVTYWx0ZWRfX9qTtGQ7/u4oBnscsUb9FSoIG97pOK1+4bZTloSUhybXy2WFbs5UgQY+zzSSp3+GZt+x5w97kT12MExK1QuzJvflEi1Afj3VOCOK3wA91KtPRPIIvkgJs3W+3WdOeSh2hGtvgRXGOOkjx9jpm89Z+hVQ6OtSoDswWNDFl8GUtSMTtAUucgtJT3karNcr5+l7PfCMaxLqy9fFXGalZnQuSAXzHvmmPcD1IBR7E5b2Pqkw2pXGThJvf8qSnBD7JHjAlwcfhb/dk681Lzu6o8ekwbBCePrepvWy1YbJLWHoA3f6WtqHKLMZS/SdowtFBXltp39kXSOnepmQ1jNpzEyNcZLgpaEBXFxzxoN+aE33048NpDXU2uERhz3bp8z2uu0vHzeQgZK9AGp9XUCq6Gblq9aUgbV3ZtlKTkr0oGOQXc0JI1Uq7wWmqb+AnDZJcOMub05YB7qR3O0QDrJPTX+k9tkDa/oICeQsDLF79yPPUfFvn9Q2Q0UBzYXQFkqjCkn6B0GeFy+LrJa6ELLZSVwhFvLyT46cyiqQ6vd+S1ajVbmfDPvfi6Ujs1nJPId/PAdkju6PcW3/1iptJLg1gvgQHQqMq+ifNMHsvwWZuCfsn2jSIcCBUekHvovwkMB+K0pMICTmgVLRPMRmgptijRuptF2b/9aBSYYYGIpyMdYyrZSS1izn33vLokk=");

        architectView.onCreate(configuration);
        setContentView(architectView);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        architectView.onPostCreate();

        try{
            architectView.load("index.html");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        architectView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        architectView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        architectView.onDestroy();
    }
}
