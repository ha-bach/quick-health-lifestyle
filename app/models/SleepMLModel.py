from sklearn.ensemble import GradientBoostingRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
import pandas as pd
import joblib


# Load your dataset
data = pd.read_csv(
    "C:\\Users\\idhan\\cs125project\\Quick-Health-Lifestyle\\app\\data\\sleep_dataset.csv"
)


X = data.drop(columns=["WakeupTime"])  # Assuming 'wakeup_time' is the target variable
y = data["WakeupTime"]

# One-hot encoding
X = pd.get_dummies(X)

# Split data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)


gbm = GradientBoostingRegressor(
    n_estimators=100, learning_rate=0.1, max_depth=3, random_state=42
)
gbm.fit(X_train, y_train)

# Predict on the test set
y_pred = gbm.predict(X_test)

# Evaluate the model
# mse = mean_squared_error(y_test, y_pred)
# print("Mean Squared Error:", mse)

# Save the model
joblib.dump(gbm, "model.joblib")
