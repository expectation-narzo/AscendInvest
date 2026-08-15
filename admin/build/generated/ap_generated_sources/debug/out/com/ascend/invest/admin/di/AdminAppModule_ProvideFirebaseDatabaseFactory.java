package com.ascend.invest.admin.di;

import com.google.firebase.database.FirebaseDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AdminAppModule_ProvideFirebaseDatabaseFactory implements Factory<FirebaseDatabase> {
  @Override
  public FirebaseDatabase get() {
    return provideFirebaseDatabase();
  }

  public static AdminAppModule_ProvideFirebaseDatabaseFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FirebaseDatabase provideFirebaseDatabase() {
    return Preconditions.checkNotNullFromProvides(AdminAppModule.provideFirebaseDatabase());
  }

  private static final class InstanceHolder {
    private static final AdminAppModule_ProvideFirebaseDatabaseFactory INSTANCE = new AdminAppModule_ProvideFirebaseDatabaseFactory();
  }
}
