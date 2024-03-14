import pandas as pd
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error

# Load your dataset (replace 'dataset.csv' with the path to your dataset)
data = pd.read_csv(
    "C:\\Users\\idhan\\cs125project\\Quick-Health-Lifestyle\\app\\data\\sleep_dataset.csv"
)

# Extract features from names
data["first_name_length"] = data["FirstName"].apply(len)
data["last_name_length"] = data["LastName"].apply(len)
data["total_name_length"] = data["first_name_length"] + data["last_name_length"]

# Use extracted features and other relevant features as input
X = data[
    [
        "first_name_length",
        "last_name_length",
        "total_name_length",
    ]
]  # Add other relevant features as needed

# Use 'wakeup_time' as the target variable
y = data["WakeupTime"]

# Split data into train and test sets
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# Initialize and train the Gradient Boosting Regressor
gbm = GradientBoostingRegressor(
    n_estimators=100, learning_rate=0.1, max_depth=3, random_state=42
)
gbm.fit(X_train, y_train)

# Predict on the test set
y_pred = gbm.predict(X_test)

# Evaluate the model
mse = mean_squared_error(y_test, y_pred)
print("Mean Squared Error:", mse)
