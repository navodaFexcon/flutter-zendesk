package com.codeheadlabs.zendesk;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.codeheadlabs.zendesk.pigeon.ZendeskPigeon.ChatApi;
import com.codeheadlabs.zendesk.pigeon.ZendeskPigeon.InitializeRequest;
import com.codeheadlabs.zendesk.pigeon.ZendeskPigeon.ProfileApi;
import com.codeheadlabs.zendesk.pigeon.ZendeskPigeon.SetDepartmentRequest;
import com.codeheadlabs.zendesk.pigeon.ZendeskPigeon.SetVisitorInfoRequest;
import com.codeheadlabs.zendesk.pigeon.ZendeskPigeon.StartChatRequest;
import com.codeheadlabs.zendesk.pigeon.ZendeskPigeon.VisitorNoteRequest;
import com.codeheadlabs.zendesk.pigeon.ZendeskPigeon.VisitorTagsRequest;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import java.util.ArrayList;
import zendesk.chat.Chat;
import zendesk.chat.ChatConfiguration;
import zendesk.chat.ChatEngine;
import zendesk.chat.ChatProvider;
import zendesk.chat.ProfileProvider;
import zendesk.chat.Providers;
import zendesk.chat.VisitorInfo;
import zendesk.chat.VisitorInfo.Builder;
import zendesk.messaging.MessagingActivity;
import zendesk.chat.PushNotificationsProvider;
import android.util.Log;
import java.util.List;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;
/** ZendeskPlugin */
public class ZendeskPlugin implements FlutterPlugin, ActivityAware, ChatApi, ProfileApi {

  private Context applicationContext;
  private Activity activity;

  public ZendeskPlugin() {}

  public static void registerWith(Registrar registrar) {
    ZendeskPlugin plugin = new ZendeskPlugin();
    plugin.startListening(registrar.context(), registrar.messenger());
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    startListening(binding.getApplicationContext(), binding.getBinaryMessenger());
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    applicationContext = null;
  }

  private void startListening(Context applicationContext, BinaryMessenger messenger) {
    ChatApi.setup(messenger, this);
    ProfileApi.setup(messenger, this);

    this.applicationContext = applicationContext;
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }

  @Override
  public void initialize(InitializeRequest arg) {
    if (arg.getAccountKey() == null) {
      throw new IllegalArgumentException("accountKey missing");
    }

    if (arg.getAppId() != null) {
      Chat.INSTANCE.init(applicationContext, arg.getAccountKey(), arg.getAppId());
    } else {
      Chat.INSTANCE.init(applicationContext, arg.getAccountKey());
    }
    if (arg.getDeviceToken() == null) {
      Log.e("onError", "NOOO");
    } else {
      PushNotificationsProvider pushProvider = Chat.INSTANCE.providers().pushNotificationsProvider();
      if (pushProvider != null) {
        Log.e("onSuccess", "Push Provider");
        pushProvider.registerPushToken(arg.getDeviceToken(), (ZendeskCallback) (new ZendeskCallback() {
          public void onSuccess( Void p0) {
            Log.e("onSuccess", "Success");
          }
          public void onSuccess(Object var1) {
            this.onSuccess((Void)var1);
          }

          public void onError( ErrorResponse p0) {
            Log.e("onError", "NOOO");
          }
        }));

      } else {
        Log.e("PushNotificationsProvider", "NULLL");
      }
      Log.e("onSuccess", arg.getDeviceToken() );
    }
  }

  @Override
  public void setDepartment(SetDepartmentRequest arg) {
    Providers providers = Chat.INSTANCE.providers();

    if (providers == null) {
      throw new IllegalArgumentException("providers not set - did you call initialize?");
    }

    ChatProvider chatProvider = providers.chatProvider();
    chatProvider.setDepartment(arg.getDepartment(), null);
  }

  @Override
  public void startChat(StartChatRequest arg) {
    if (activity == null) {
      return;
    }

    ChatConfiguration chatConfiguration =
        ChatConfiguration.builder().withAgentAvailabilityEnabled(false).build();

    MessagingActivity.builder().withEngines(ChatEngine.engine()).show(activity, chatConfiguration);
  }

  @Override
  public void setVisitorInfo(SetVisitorInfoRequest arg) {
    final ProfileProvider profileProvider = getProfileProvider();

    Builder builder = VisitorInfo.builder();
    if (!TextUtils.isEmpty(arg.getName())) {
      builder = builder.withName(arg.getName());
    }
    if (!TextUtils.isEmpty(arg.getEmail())) {
      builder = builder.withEmail(arg.getEmail());
    }
    if (!TextUtils.isEmpty(arg.getPhoneNumber())) {
      builder = builder.withPhoneNumber(arg.getPhoneNumber());
    }

    profileProvider.setVisitorInfo(builder.build(), null);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void addVisitorTags(VisitorTagsRequest arg) {
    final ProfileProvider profileProvider = getProfileProvider();
    List<Object> raw = arg.getTags();
    ArrayList<String> tags = new ArrayList<>();
    for (Object o : raw) {
      if (o instanceof String) {
        tags.add((String) o);
      }
    }
    profileProvider.addVisitorTags(tags, null);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void removeVisitorTags(VisitorTagsRequest arg) {
    final ProfileProvider profileProvider = getProfileProvider();
    List<Object> raw = arg.getTags();
    ArrayList<String> tags = new ArrayList<>();
    for (Object o : raw) {
      if (o instanceof String) {
        tags.add((String) o);
      }
    }
    profileProvider.removeVisitorTags(tags, null);
  }

  @Override
  public void setVisitorNote(VisitorNoteRequest arg) {
    final ProfileProvider profileProvider = getProfileProvider();
    profileProvider.setVisitorNote(arg.getNote());
  }

  @Override
  public void appendVisitorNote(VisitorNoteRequest arg) {
    final ProfileProvider profileProvider = getProfileProvider();
    profileProvider.appendVisitorNote(arg.getNote());
  }

  @Override
  public void clearVisitorNotes() {
    final ProfileProvider profileProvider = getProfileProvider();
    profileProvider.clearVisitorNotes(null);
  }

  private ProfileProvider getProfileProvider() {
    Providers providers = Chat.INSTANCE.providers();
    if (providers == null) {
      throw new IllegalArgumentException("providers not set - did you call initialize?");
    }

    return providers.profileProvider();
  }
}
