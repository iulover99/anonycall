package com.example.anonycall.fragments;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.anonycall.MainActivity;
import com.example.anonycall.R;
import com.example.anonycall.databinding.LoginFragmentBinding;
import com.example.anonycall.viewModels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class LoginFragment extends Fragment {
    private static final int REQUEST_CODE = 101010;
    private final String TAG = "LoginFragment";
    private UserViewModel mUserViewModel;
    private LoginFragmentBinding binding;
//    private GoogleSignInOptions gso;
//    private GoogleSignInClient gsoClient;


    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;
    ProgressDialog progressDialog;
    private ImageView imageViewLogin;
    private Executor executor;
    private BiometricPrompt biometricPrompt;

    private androidx.biometric.BiometricPrompt.PromptInfo promptInfo;
    SharedPreferences sharedPreferences;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hiding bottom nav
        ((MainActivity) requireActivity()).hidingBottomNavigation(true);
//        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.anony_web_client_id))
//                .requestEmail()
//                .build();
//        gsoClient = GoogleSignIn.getClient(requireContext(),gso);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LoginFragmentBinding.inflate(inflater,container,false);

        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.progressHorizontal.setVisibility(View.INVISIBLE);

        binding.loginBtn.setOnClickListener(v -> {
            binding.progressHorizontal.setIndeterminate(true);
            binding.progressHorizontal.setVisibility(View.VISIBLE);

            String emailString = binding.textinputlayoutEmail.getEditText().getText().toString();
            String passwordString = binding.textinputlayoutPassword.getEditText().getText().toString();

            LoginWithAccount(emailString, passwordString);
        });
        binding.signUpTextview.setOnClickListener(v -> goToSignupFragment());


        progressDialog = new ProgressDialog(requireActivity());
        sharedPreferences = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        boolean isLogin = sharedPreferences.getBoolean("isLogin", false);

        BiometricManager biometricManager = BiometricManager.from(requireActivity());
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "???ng d???ng c?? th??? s??? d???ng sinh tr???c h???c!");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(requireContext(), "Ch??a c?? v??n tay", Toast.LENGTH_SHORT).show();
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(requireContext(), "C???m bi???n kh??ng s???n c??!", Toast.LENGTH_SHORT).show();
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                //startActivityForResult(enrollIntent, REQUEST_CODE); //com men g?? ??
                break;
        }

        executor = ContextCompat.getMainExecutor(requireActivity());
        biometricPrompt = new BiometricPrompt(LoginFragment.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(requireContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                String emailString = sharedPreferences.getString("email", "");
                String passwordString = sharedPreferences.getString("password", "");

                LoginWithAccount(emailString, passwordString);

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(requireContext(), "X??c th???c th???t b???i",
                        Toast.LENGTH_SHORT).show();

            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("????ng nh???p v??n tay cho AnonyApp")
                .setSubtitle("????ng nh???p s??? d???ng ch??a kho?? v??n tay")
                .setNegativeButtonText("S??? d???ng m???t kh???u ????? ????ng nh???p")
                .build();
        imageViewLogin = binding.icon;
        if (isLogin) {
            imageViewLogin.setVisibility(View.VISIBLE);
        }

        imageViewLogin.setOnClickListener(view1 -> {
            biometricPrompt.authenticate(promptInfo);
        });
//        binding.loginGoogle.setOnClickListener(v -> {
//            LoginWithGoogle();
//        });
    }

//    private void LoginWithGoogle() {
//        Intent signInIntent = gsoClient.getSignInIntent();
//        activityResultLauncher.launch(signInIntent);
//    }

//    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if(result.getResultCode() == Activity.RESULT_OK) {
//                    Intent data = result.getData();
//                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                    try {
//                        // Google Sign In was successful, authenticate with Firebase
//                        GoogleSignInAccount account = task.getResult(ApiException.class);
//                        Log.e("GOOGLE LOGIN:", "firebaseAuthWithGoogle:" + account.getId());
//                        firebaseAuthWithGoogle(account.getIdToken());
//                    } catch (ApiException e) {
//                        // Google Sign In failed, update UI appropriately
//                        binding.loginBtn.setClickable(true);
//                        binding.progressHorizontal.setVisibility(View.INVISIBLE);
//                        Log.e("GOOGLE LOGIN", "Google sign failed", e);
//                        Toast.makeText(requireContext(), "Google login failed!", Toast.LENGTH_LONG).show();
//                    }
//                } else {
//                    binding.loginBtn.setClickable(true);
//                    binding.progressHorizontal.setVisibility(View.INVISIBLE);
//                    Log.e("ACTIVITY_RESULT","ResultCode:" + result.getResultCode());
//                }
//            }
//    );

//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Sign in success, update UI with the signed-in user's information
//                        Log.e("GOOGLE LOGIN", "signInWithCredential:success");
//                        // TODO: V?? m??n ch??nh ??? ????y
//                        goToMainFragment();
//                    } else {
//                        // If sign in fails, display a message to the user.
//                        binding.loginBtn.setClickable(true);
//                        binding.progressHorizontal.setVisibility(View.INVISIBLE);
//                        Log.e("GOOGLE LOGIN", "signInWithCredential:failure", task.getException());
//                        Toast.makeText(requireContext(), "SignIn with credential failed!", Toast.LENGTH_LONG).show();
//                    }
//                });
//    }


    private void LoginWithAccount(String emailString, String passwordString) {
        try {
            binding.loginBtn.setEnabled(false);
            if (
                    binding.textinputlayoutEmail.getEditText() == null ||
                    binding.textinputlayoutPassword.getEditText() == null
            ) {
                throw new NullPointerException();
            }

            if(emailString.isEmpty()){
                binding.textinputlayoutEmail.setError(getString(R.string.email_err));
                throw new Exception("Email c???a b???n ch??a ????ng ?????nh d???ng ho???c ??ang ????? tr???ng");
            } else {
                binding.textinputlayoutEmail.setError(null);
            }
            if (passwordString.isEmpty() || passwordString.length() < 4){
                binding.textinputlayoutPassword.setError(getString(R.string.pass_err));
                throw new Exception("M???t kh???u c???a b???n qu?? ng???n");
            } else {
                binding.textinputlayoutPassword.setError(null);
            }
            auth.signInWithEmailAndPassword(emailString,passwordString).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Log.e(TAG,"????ng nh???p th??nh c??ng");

                    SharedPreferences.Editor editor = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                    editor.putString("email", emailString);
                    editor.putString("password", passwordString);
                    editor.putBoolean("isLogin", true);
                    editor.apply();

                    mUserViewModel.getInitialUser();
                    goToMainFragment();
                } else {
                    Log.e(TAG, "signInWithEmailAndPassword:failure", task.getException());
                    Toast.makeText(requireContext(), "Email ho???c m???t kh???u kh??ng ????ng, ho???c t??i kho???n kh??ng t???n t???i",
                            Toast.LENGTH_SHORT).show();
                    binding.loginBtn.setEnabled(true);
                    binding.progressHorizontal.setVisibility(View.INVISIBLE);
                }
            });

        } catch (NullPointerException ex){
            Log.e(TAG, "NullPointerException Raise: ", ex);
            binding.loginBtn.setEnabled(true);
            binding.progressHorizontal.setVisibility(View.INVISIBLE);
        } catch (Exception ex) {
            Toast.makeText(requireContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            Log.e(TAG, "Exception Raise: ", ex);
            binding.loginBtn.setEnabled(true);
            binding.progressHorizontal.setVisibility(View.INVISIBLE);
        }
    }

    private void goToSignupFragment() {
        Navigation.findNavController(requireView()).navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment());
    }

    // TODO: Di chuy???n ?????n m??n h??nh ch??nh
    private void goToMainFragment() {
        Navigation.findNavController(requireView()).navigate(LoginFragmentDirections.actionLoginFragmentToWelcomeFragment());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}