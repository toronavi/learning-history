# Hilt

Hiltは、Daggerを基盤としたAndroid向けの依存関係注入（DI）ライブラリです。定型的なコードを削減し、より簡単にDIを導入できるように設計されています。

## 1. セットアップ

**a. `build.gradle` (プロジェクトレベル)**
```groovy
plugins {
    id 'com.google.dagger.hilt.android' version '2.48' apply false
}
```

**b. `build.gradle` (モジュールレベル)**
```groovy
plugins {
    id 'com.google.dagger.hilt.android'
    id 'kotlin-kapt'
}

android {
    // ...
}

dependencies {
    implementation "com.google.dagger:hilt-android:2.48"
    kapt "com.google.dagger:hilt-compiler:2.48"
}
```

## 2. Applicationクラスの設定

Applicationクラスに `@HiltAndroidApp` アノテーションを付与します。

```kotlin
import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    // ...
}
```
`AndroidManifest.xml` でこのApplicationクラスを指定するのを忘れないようにしてください。

```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

## 3. ActivityやFragmentへの注入

`@AndroidEntryPoint` アノテーションを付けることで、Activity、Fragment、View、ServiceなどでDIが可能になります。

```kotlin
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var analyticsAdapter: AnalyticsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // analyticsAdapter をここで利用できる
    }
}
```

## 4. モジュールの作成と依存関係の提供

コンストラクタインジェクションが使えないクラス（例: `Retrofit`のような外部ライブラリのクラスや、インターフェース）のインスタンスを提供するには、Hiltモジュールを作成します。

**a. モジュールの定義**
`@Module` と `@InstallIn` アノテーションを使ってモジュールを作成します。`@InstallIn` は、そのモジュールがどのHiltコンポーネントに属するかを指定します。

`SingletonComponent::class` を指定すると、アプリケーション全体でシングルトンとしてインスタンスが提供されます。

```kotlin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ダミーの分析用クラス
class AnalyticsAdapter {
    fun logEvent(eventName: String) {
        println("Event: $eventName")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsAdapter(): AnalyticsAdapter {
        return AnalyticsAdapter()
    }
}
```

上記のように設定することで、`@AndroidEntryPoint` が付与されたクラス内で `@Inject` を使って `AnalyticsAdapter` のインスタンスを受け取ることができるようになります。
